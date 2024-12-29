package ir.rbp.nab.kafka.consumer;

import ir.rbp.nab.common.enumeration.EN_KafkaLogActionType;
import ir.rbp.nab.common.enumeration.EN_KafkaLogStatus;
import ir.rbp.nab.model.domainmodel.kafka.KafkaConsumerBranchPostLog;
import ir.rbp.nab.model.dto.externalservice.org.org.OrgChartBranchOrgPostNewDTO;
import ir.rbp.nab.service.kafka.IKafkaConsumerBranchPostLogService;
import ir.rbp.nab.service.organizing.IORGDefinedRolePlayingSetService;
import ir.rbp.nab.service.organizing.IORGMembershipOrganizingService;
import ir.rbp.nabcore.model.dto.ZoneCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final IORGMembershipOrganizingService iorgMembershipOrganizingService;
    private final IKafkaConsumerBranchPostLogService iKafkaConsumerBranchPostLogService;
    private final IORGDefinedRolePlayingSetService iorgDefinedRolePlayingSetService;

    private static ZoneCode getZoneCode() {
       return new ZoneCode(-1L);
    }

    @KafkaListener(topics = "${topic.org.branch.post.insert}", groupId = "org-nab-grp", containerFactory = "orgPostKafkaListenerContainerFactory")
    public void kafkaListenerPersistNewBranchPost(@Payload OrgChartBranchOrgPostNewDTO orgChartBranchOrgPostNewDTO, @Header("messageId") String messageId) {

        if (checkDuplicateMessage(messageId)) return;

        // insert kafka log
        var kafkaConsumerBranchPostLog = insertKafkaLogForPersistedPost(orgChartBranchOrgPostNewDTO, messageId);

        // doing process
        iorgMembershipOrganizingService.kafkaListenerPersistNewBranchPost(orgChartBranchOrgPostNewDTO, getZoneCode());

        // set finished kafka log
        iKafkaConsumerBranchPostLogService.updateKafkaConsumerBranchPostLogByStatusId(kafkaConsumerBranchPostLog, EN_KafkaLogStatus.FINISHED.getId());
    }

    private KafkaConsumerBranchPostLog insertKafkaLogForPersistedPost(OrgChartBranchOrgPostNewDTO orgChartBranchOrgPostNewDTO, String messageId) {
        // get count of defined role playing set that will be effected for set in kafka log
        Long defRpsCount = iorgDefinedRolePlayingSetService.getCountOfDefRpsByChartId(orgChartBranchOrgPostNewDTO.getChartId());

        // kafka action type (persist new post/persist sub post)
        EN_KafkaLogActionType kafkaLogActionType = orgChartBranchOrgPostNewDTO.getParentId() == null ? EN_KafkaLogActionType.PERSIST_NEW_POST: EN_KafkaLogActionType.UPDATE_SUB_POST;

        return iKafkaConsumerBranchPostLogService.persistKafkaConsumerBranchPostLog(
                orgChartBranchOrgPostNewDTO.getId(), messageId, kafkaLogActionType, defRpsCount, getZoneCode());
    }

    @KafkaListener(topics = "${topic.org.branch.post.update.repetitive}", groupId = "org-nab-grp", containerFactory = "orgPostKafkaListenerContainerFactory")
    public void kafkaListenerUpdateBranchPostRepetitiveValues(@Payload OrgChartBranchOrgPostNewDTO orgChartBranchOrgPostNewDTO, @Header("messageId") String messageId) {

        if (checkDuplicateMessage(messageId)) return;

        // get count of defined role playing set that will be effected for set in kafka log
        Long defRpsCount = iorgDefinedRolePlayingSetService.getCountOfDefRpsByChartId(orgChartBranchOrgPostNewDTO.getChartId());

        // insert kafka log
        KafkaConsumerBranchPostLog kafkaConsumerBranchPostLog = iKafkaConsumerBranchPostLogService.persistKafkaConsumerBranchPostLog(
                orgChartBranchOrgPostNewDTO.getId(), messageId, EN_KafkaLogActionType.UPDATE_TO_REPETITIVE_POST, defRpsCount, getZoneCode());

        // doing process
        iorgMembershipOrganizingService.kafkaListenerUpdateBranchPostRepetitiveValues(orgChartBranchOrgPostNewDTO);

        // set finished kafka log
        iKafkaConsumerBranchPostLogService.updateKafkaConsumerBranchPostLogByStatusId(kafkaConsumerBranchPostLog, EN_KafkaLogStatus.FINISHED.getId());
    }

    @KafkaListener(topics = "${topic.org.branch.post.delete}", groupId = "org-nab-grp", containerFactory = "orgPostKafkaListenerContainerFactory")
    public void kafkaListenerDeleteBranchPost(@Payload OrgChartBranchOrgPostNewDTO orgChartBranchOrgPostNewDTO, @Header("messageId") String messageId) {

        if (checkDuplicateMessage(messageId)) return;

        // get count of organizing that will be effected for set in kafka log
        Long countOrganizingShouldRemove = iorgMembershipOrganizingService.getCountOrganizingShouldRemoveByBranchPostId(orgChartBranchOrgPostNewDTO.getId());

        // insert kafka log
        KafkaConsumerBranchPostLog kafkaConsumerBranchPostLog = iKafkaConsumerBranchPostLogService.persistKafkaConsumerBranchPostLog(
                orgChartBranchOrgPostNewDTO.getId(), messageId, EN_KafkaLogActionType.DELETE_POST, countOrganizingShouldRemove, getZoneCode());

        // doing process
        iorgMembershipOrganizingService.kafkaListenerDeleteBranchPost(orgChartBranchOrgPostNewDTO);

        // set finished kafka log
        iKafkaConsumerBranchPostLogService.updateKafkaConsumerBranchPostLogByStatusId(kafkaConsumerBranchPostLog, EN_KafkaLogStatus.FINISHED.getId());
    }


    private boolean checkDuplicateMessage(String messageId){
        if(iKafkaConsumerBranchPostLogService.findByMessageId(messageId) != null) {
            log.warn("\u001B[31m ********** Found a duplicate message id: {} \u001B[0m", messageId);
            return true;
        }
        return false;
    }
}
