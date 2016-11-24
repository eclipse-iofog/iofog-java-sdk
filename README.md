# container-sdk-java

This module lets you easily build an ioElement. It gives you all the functionality to interact with ioFog via Local API. Additionally some useful methods to work with ioMessage.

 - send new message to ioFog (pushNewMessage)
 - fetch next unread messages from ioFog (fetchNextMessage)
 - fetch messages for time period and list of accessible publishers (fetchMessagesByQuery)
 - get config options (fetchContainerConfig)
 - connect to ioFog Control Channel via WebSocket (openControlWebSocket)
 - connect to ioFog Message Channel via WebSocket (openMessageWebSocket) and publish new message via this channel (sendMessageToWebSocket)

## Code snippets: 

`ioFogClient` implements all methods to communicate with ioFog (via local API).
`IOMessage` represent all message communication between ioFog and Containers.
`IOMessageUtils` class is convenient to encode and decode byte arrays.
`ioFogAPIListener` - listener's interface for requests to ioFog.

set up custom host, port and container's ID (in case of no params default values for host and port will be used: 'ioFog', 54321):
```java
   ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
```

#### REST calls
post new ioMessage to ioFog via REST call (could trigger onError, onBadRequest and onMessageReceipt listener's methods):
```java
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFogListener = new MyIOFAPIListener(); // implementation of listener's interface
  ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
  ioFogClient.pushNewMessage(message, ioFogListener);
```

get list of ioMessages by time frame for accessible publishers from ioFog via REST call (could trigger onError, onBadRequest and onMessagesQuery listener's methods):
```java
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFogListener = new MyIOFAPIListener(); // implementation of listener's interface
  ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
  ioFogClient.fetchMessagesByQuery(new Date(), new Date(), Collections.singleton("publisher_ID"), ioFogListener);
```

get list of next unread ioMessages via REST call (could trigger onError, onBadRequest and onMessages listener's methods):
```java
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFogListener = new MyIOFAPIListener(); // implementation of listener's interface
  ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
  ioFogClient.fetchNextMessage(ioFogListener);
```

get container's config via REST call (could trigger onError, onBadRequest and onNewConfig listener's methods):
```java
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFogListener = new MyIOFAPIListener(); // implementation of listener's interface
  ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
  ioFogClient.fetchContainerConfig(ioFogListener);
```

#### WebSocket(WS) calls

Open WS Control Channel to ioFog (could trigger onError and onNewConfigSignal listener's methods):
```java
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFogListener = new MyIOFAPIListener(); // implementation of listener's interface
  ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
  ioFogClient.openControlWebSocket(ioFogListener);
```
Open WS Message Channel to ioFog (could trigger onError, onMessages and onMessageReceipt listener's methods):
```java
  IOMessage message = new IOMessage();
  MyIOFAPIListener ioFogListener = new MyIOFAPIListener(); // implementation of listener's interface
  ioFogClient ioFogClient = new ioFogClient("iofog", 54321, "TEST_CONTAINER_ID");
  ioFogClient.openMessageWebSocket(ioFogListener);
```
Send ioMessage via WS Message Channel (pre-condition: WS Message Channel is open):
```java
  IOMessage message = new IOMessage();
  ioFogClient.sendMessageToWebSocket(message);
```





