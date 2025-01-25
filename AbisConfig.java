package ir.naji.abis.ai;

import ir.jame.core.i18n.ServerMessagesUtil;
import ir.naji.abis.AbisRemoteExceptionRegistry;
import ir.naji.abis.ai.tools.BlockingMap;
import ir.naji.abis.facerequest.FaceRequestEntity;
import ir.naji.abis.request.RequestStatus;
import ir.naji.abis.request.SearchResultDTO;
import ir.naji.abis.request.SearchSingleResultDTO;
import ir.naji.abis.tools.SearchEnginesZMQWrapperImpl;
import ir.naji.abis.tools.ZMQWrapperImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FaceRequestDispatcher {

	@Value("${searchEngine.search.timeout}")
	public Integer timeout;
	@Value("${faceRequest.result.size}")
	public Integer limit;
	@Value("${faceRequest.maxGeneratedId}")
	public Integer maxId;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private SearchEnginesZMQWrapperImpl searchEnginesZMQWrapper;

	// SearchEngineName, RequestPriorityQueue
	private Map<String, PriorityBlockingQueue<SearchEngineRequestDTO>> searchEnginesPriorityQueues;

	// RequestId, SearchEngineName, RelatedEngineResult
	private Map<String, Map<String, SearchEngineResultDTO>> searchEnginesResultMap;

	// RequestId, SearchEngineNames
	private Map<String, List<String>> requestsSearchEngines;

	// RequestId, RelatedEngineResult
	private BlockingMap<String, SearchResultDTO> searchEnginesUnionResultMap;

	// SearchEngineName, Runnable
	private Map<String, Thread> searchEnginesDispatcherThreads;

	public FaceRequestDispatcher(SearchEnginesZMQWrapperImpl searchEnginesZMQWrapper) {
		this.searchEnginesZMQWrapper = searchEnginesZMQWrapper;
		searchEnginesPriorityQueues = new ConcurrentHashMap<>(searchEnginesZMQWrapper.getZmqWrapperForSearchEngines().size());
		searchEnginesResultMap = new ConcurrentHashMap<>();
		requestsSearchEngines = new ConcurrentHashMap<>();
		searchEnginesUnionResultMap = new BlockingMap<>();
		searchEnginesDispatcherThreads = new ConcurrentHashMap<>(searchEnginesZMQWrapper.getZmqWrapperForSearchEngines().size());
		logger.info("search engine beans" + searchEnginesZMQWrapper.getZmqWrapperForSearchEngines());
		for (String key : searchEnginesZMQWrapper.getZmqWrapperForSearchEngines().keySet()) {
			searchEnginesPriorityQueues.put(key, new PriorityBlockingQueue<>());
			searchEnginesDispatcherThreads.put(key, new Thread(new DispatcherQueueHandler(key)));
			searchEnginesDispatcherThreads.get(key).start();
		}
	}

	public SearchResultDTO processSearchRequest(FaceRequestEntity faceRequestEntity) throws InterruptedException {
		String requestId = String.valueOf(new Random().nextInt(maxId));
		for (String engineType : faceRequestEntity.getEngineTypesList()) {
			SearchEngineRequestDTO searchEngineRequestDTO = new SearchEngineRequestDTO(requestId,
					SearchEngineRequestDTO.SearchEngineRequestType.SEARCH,
					limit, faceRequestEntity.getPriority(), faceRequestEntity.getFeature());
			if (!searchEnginesPriorityQueues.get(engineType).offer(searchEngineRequestDTO, timeout, TimeUnit.SECONDS)) {
				throw AbisRemoteExceptionRegistry.create(AbisRemoteExceptionRegistry.SEARCH_QUEUE_IS_FULL, ServerMessagesUtil.getMessage("ir.naji.abis.errorMessages", "searchQueueIsFull"));
			}
		}
		requestsSearchEngines.put(requestId, faceRequestEntity.getEngineTypesList());

		logger.info("Search request queue size: " + requestsSearchEngines.keySet().size());

		SearchResultDTO remove = searchEnginesUnionResultMap.remove(requestId, timeout, TimeUnit.SECONDS);
		return remove;
	}

	private class DispatcherQueueHandler implements Runnable {
		private String engineName;
		private ObjectMapper objectMapper;

		public DispatcherQueueHandler(String engineName) {
			this.engineName = engineName;
			this.objectMapper = new ObjectMapper();
		}

		@Override
		public void run() {
			boolean running = true;
			while (running) {
				try {
					SearchEngineRequestDTO searchEngineRequestDTO = searchEnginesPriorityQueues.get(engineName).take();
					ZMQWrapperImpl searchEngineZmqWrapper = searchEnginesZMQWrapper.getZmqWrapperForSearchEngines().get(engineName);
					String searchEngineResponse = searchEngineZmqWrapper.getResponseOf(objectMapper.writeValueAsString(Arrays.asList(searchEngineRequestDTO)));
					logger.trace("Search engine response: " + searchEngineResponse);
					List<SearchEngineResultDTO> searchEngineResultDTOS = objectMapper.readValue(searchEngineResponse, new TypeReference<List<SearchEngineResultDTO>>() {
					});
					if (searchEnginesResultMap.get(searchEngineRequestDTO.getId()) == null) {
						searchEnginesResultMap.put(searchEngineRequestDTO.getId(), new ConcurrentHashMap<>());
					}
					searchEnginesResultMap.get(searchEngineRequestDTO.getId()).put(engineName, searchEngineResultDTOS.get(0));

					if (searchEnginesResultMap.get(searchEngineRequestDTO.getId()).size() == requestsSearchEngines.get(searchEngineRequestDTO.getId()).size()) {
						unionResults(searchEngineRequestDTO.getId());
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
					running = false;
					Thread.currentThread().interrupt();
				} catch (JsonProcessingException e) {
					logger.error(e.getMessage(), e);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private void unionResults(String id) {
		SearchResultDTO searchResultDTO = new SearchResultDTO(new Date().getTime() + (timeout*1000), Long.valueOf(id));
		for (SearchEngineResultDTO searchEngineResult : searchEnginesResultMap.get(id).values()) {
			if (searchEngineResult.getStatus()) {
				searchResultDTO.setStatus(RequestStatus.DONE.name());
				searchResultDTO.setErrorDescription(searchEngineResult.getDescription());
				for (int i = 0; i < searchEngineResult.getIds().size(); i++) {
					searchResultDTO.getResults().add(new SearchSingleResultDTO(searchEngineResult.getIds().get(i),
							Float.valueOf(searchEngineResult.getSimilarities().get(i))));
				}
			} else if (searchResultDTO.getStatus() == null) {
				searchResultDTO.setStatus(RequestStatus.ERROR.name());
				searchResultDTO.setErrorDescription(searchEngineResult.getDescription());
			}
		}
		searchResultDTO.setResults(searchResultDTO.getResults().stream().sorted(Collections.reverseOrder()).limit(limit).collect(Collectors.toList()));
		searchEnginesUnionResultMap.put(searchResultDTO.getId().toString(), searchResultDTO);
		requestsSearchEngines.remove(id);
	}
}
