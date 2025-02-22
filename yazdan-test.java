private String getResponseOf(String request, int retryCount) {
		if (retryCount++ > MAX_RETRY) {
			throw AbisRemoteExceptionRegistry.create(AbisRemoteExceptionRegistry.ERROR_IN_SENDING_ZMQ_MESSAGE, ServerMessagesUtil.getMessage("ir.naji.abis.errorMessages", "errorInSendingZMQMessage"));
		}
		ZMQ.Socket socket = getSocket();
		String result = null;
		try {
			socket.send(request, 0);
			result = socket.recvStr(0);
		} catch (ZMQException e) {
			logger.error(e.getMessage());
			logger.trace(String.format("Exception in ZMQtoPythonClient: caused by class (%s) and JSON (%s) ", e.getClass(), request != null ? request : " null "));
			return getResponseOf(request, retryCount);
		} finally {
			destroySocket(socket);
		}
		return result;
	}

I have this code for create a connection socket but I have a problem when connection to database is drop I want to try again for connection to it , I know my db is up but connection is not connect again until i rest the module . see this code and tell me how can solve this problem and consider it has recursive call
