# container-sdk-java

This module lets you easily build an ioElement. It gives you all the functionality to interact with ioFabric via Local API. Additionally some useful methods to work with ioMessage.

 - send new message to ioFabric (pushNewMessage)
 - fetch next unread messages from ioFabric (fetchNextMessage)
 - fetch messages for time period and list of accessible publishers (fetchMessagesByQuery)
 - get config options (fetchContainerConfig)
 - connect to ioFabric Control Channel via WebSocket (openControlWebSocket)
 - connect to ioFabric Message Channel via WebSocket (openMessageWebSocket) and publish new message via this channel (sendMessageToWebSocket)

## Code snippets: 

`IOFabricClient` implements all methods to communicate with ioFabric (via local API).
`IOMessage` represent all message communication between ioFabric and Containers.
`IOMessageUtils` class is convenient to encode and decode byte arrays.
`IOFabricAPIListener` - listener's interface for requests to ioFabric.

set up custom host, port and container's ID (in case of no params default values for host and port will be used: 'iofabric', 54321):
```
   IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
```

#### REST calls
post new ioMessage to ioFabric via REST call (could trigger onError, onBadRequest and onMessageReceipt listener's methods):
```
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFabricListener = new MyIOFAPIListener(); // implementation of listener's interface
  IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
  ioFabricClient.pushNewMessage(message, ioFabricListener);
```

get list of ioMessages by time frame for accessible publishers from ioFabric via REST call (could trigger onError, onBadRequest and onMessagesQuery listener's methods):
```
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFabricListener = new MyIOFAPIListener(); // implementation of listener's interface
  IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
  ioFabricClient.fetchMessagesByQuery(new Date(), new Date(), Collections.singleton("publisher_ID"), ioFabricListener);
```

get list of next unread ioMessages via REST call (could trigger onError, onBadRequest and onMessages listener's methods):
```
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFabricListener = new MyIOFAPIListener(); // implementation of listener's interface
  IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
  ioFabricClient.fetchNextMessage(ioFabricListener);
```

get container's config via REST call (could trigger onError, onBadRequest and onNewConfig listener's methods):
```
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFabricListener = new MyIOFAPIListener(); // implementation of listener's interface
  IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
  ioFabricClient.fetchContainerConfig(ioFabricListener);
```

#### WebSocket(WS) calls

Open WS Control Channel to ioFabric (could trigger onError and onNewConfigSignal listener's methods):
```
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFabricListener = new MyIOFAPIListener(); // implementation of listener's interface
  IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
  ioFabricClient.openControlWebSocket(ioFabricListener);
```
Open WS Message Channel to ioFabric (could trigger onError, onMessages and onMessageReceipt listener's methods):
```
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFabricListener = new MyIOFAPIListener(); // implementation of listener's interface
  IOFabricClient ioFabricClient = new IOFabricClient("127.0.0.1", 10500, "TEST_CONTAINER_ID");
  ioFabricClient.openMessageWebSocket(ioFabricListener);
```
Send ioMessage via WS Message Channel (pre-condition: WS Message Channel is open):
```
  IOMessage message = new IOMessage();
  ioFabricClient.sendMessageToWebSocket(message);
```





