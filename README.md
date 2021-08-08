# Messaging

A simple-to-use messaging system.

# Example

Example simple plugin found in [ExamplePlugin](./src/main/java/dev/sergivos/exampleplugin/)

```java
// Create and (un)register packets
PacketManager packetManager = new PacketManager();
packetManager.register(SimplePacket.class,SimplePacket::new);
packetManager.unregister(OldPacket.class);

// Create our broker/pubsub
final NatsBroker broker=new NatsBroker(
"nats://127.0.0.1:4222,nats://127.0.0.1:5222,nats://127.0.0.1:6222");

// Create service
MessagingService messagingService = new MessagingService("TestPlugin",packetManager,broker);

// Register events
messagingService.registerListener(new PacketListener());
```