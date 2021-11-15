## This Repo demonstrates a Bug in http4s

When the default `ContextShift` and `Timer` from `cats.effect.IOApp` is passed to `BlazeServerBuilder`,
if there's a `java.lang.Error` raised during the processing of one of the HTTP Requests, 
the error bubbles up to the main thread running the server and kills the app. 

This behaviour doesn't seem to happen if the user defines their own `ContextShift` and `Timer`


### Scenario 1 - Implicits from IOApp are used

When the `org.http4s.blaze.server.BlazeServerBuilder` is used to bootstrap the http4s server inside a
class that extends the `cats.effect.IOApp`, if there is a non-catchable JVM error raised in one of the routes, the app starts to
shut down. It also ends up calling `.guarantee` if it is defined on the `IO`.

### Scenario 2 - Custom Implicits are defined

If the `org.http4s.blaze.server.BlazeServerBuilder` is used in any other class that doesn't extend the
`cats.effect.IOApp`, then we need to provide two implicit parameters `(implicit
F: ConcurrentEffect[F],
timer: Timer[F])` which in the case of the IOApp are in scope. In this case, if a JVM error is raised
it does not cause the App to shut down.


### Running the example

Run this app and try to curl the following URL

```bash
curl http://localhost:8080/hello/john
```

You will see the following logs

```
[blaze-selector-0] INFO  o.h.b.c.n.NIO1SocketServerGroup - Accepted connection from /0:0:0:0:0:0:0:1:56835 
[blaze-selector-1] INFO  o.h.b.c.n.NIO1SocketServerGroup - Accepted connection from /0:0:0:0:0:0:0:1:56836 
java.lang.StackOverflowError: BOOM BANG!!
	at com.example.catseffectbug.HelloWorld$$anon$2.$anonfun$simulateError$1(HelloWorld.scala:40)
	at cats.syntax.EitherObjectOps$.catchNonFatal$extension(either.scala:370)
	at com.example.catseffectbug.HelloWorld$$anon$2.simulateError(HelloWorld.scala:40)
	at com.example.catseffectbug.HelloWorld$$anon$2.hello(HelloWorld.scala:34)
	at com.example.catseffectbug.CatseffectbugRoutes$$anonfun$helloWorldRoutes$1.applyOrElse(CatseffectbugRoutes.scala:18)
	at com.example.catseffectbug.CatseffectbugRoutes$$anonfun$helloWorldRoutes$1.applyOrElse(CatseffectbugRoutes.scala:15)
	at scala.PartialFunction$Lifted.apply(PartialFunction.scala:313)
	at scala.PartialFunction$Lifted.apply(PartialFunction.scala:309)
	at org.http4s.HttpRoutes$.$anonfun$of$2(HttpRoutes.scala:79)
	at cats.effect.internals.IORunLoop$.liftedTree1$1(IORunLoop.scala:123)
	at cats.effect.internals.IORunLoop$.cats$effect$internals$IORunLoop$$loop(IORunLoop.scala:118)
	at cats.effect.internals.IORunLoop$.restartCancelable(IORunLoop.scala:51)
	at cats.effect.internals.IOBracket$.$anonfun$guaranteeCase$2(IOBracket.scala:126)
	at cats.effect.internals.Trampoline.cats$effect$internals$Trampoline$$immediateLoop(Trampoline.scala:67)
	at cats.effect.internals.Trampoline.startLoop(Trampoline.scala:35)
	at cats.effect.internals.TrampolineEC$JVMTrampoline.super$startLoop(TrampolineEC.scala:90)
	at cats.effect.internals.TrampolineEC$JVMTrampoline.$anonfun$startLoop$1(TrampolineEC.scala:90)
	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	at scala.concurrent.BlockContext$.withBlockContext(BlockContext.scala:94)
	at cats.effect.internals.TrampolineEC$JVMTrampoline.startLoop(TrampolineEC.scala:90)
	at cats.effect.internals.Trampoline.execute(Trampoline.scala:43)
	at cats.effect.internals.TrampolineEC.execute(TrampolineEC.scala:42)
	at cats.effect.internals.IOBracket$.$anonfun$guaranteeCase$1(IOBracket.scala:116)
	at cats.effect.internals.IOBracket$.$anonfun$guaranteeCase$1$adapted(IOBracket.scala:114)
	at cats.effect.internals.IOAsync$.$anonfun$apply$1(IOAsync.scala:37)
	at cats.effect.internals.IOAsync$.$anonfun$apply$1$adapted(IOAsync.scala:37)
	at cats.effect.internals.IORunLoop$RestartCallback.start(IORunLoop.scala:464)
	at cats.effect.internals.IORunLoop$.cats$effect$internals$IORunLoop$$loop(IORunLoop.scala:165)
	at cats.effect.internals.IORunLoop$RestartCallback.signal(IORunLoop.scala:480)
	at cats.effect.internals.IORunLoop$RestartCallback.apply(IORunLoop.scala:501)
	at cats.effect.internals.IORunLoop$RestartCallback.apply(IORunLoop.scala:439)
	at cats.effect.internals.IOShift$Tick.run(IOShift.scala:36)
	at cats.effect.internals.PoolUtils$$anon$2$$anon$3.run(PoolUtils.scala:52)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
[blaze-acceptor-0-0] INFO  o.h.b.c.ServerChannel - Closing NIO1 channel /0:0:0:0:0:0:0:0:8080 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-0 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-1 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-2 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-3 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-4 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-5 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-6 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-7 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-8 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-9 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-10 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-11 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-selector-12 
[shutdownHook1] INFO  o.h.b.c.n.SelectorLoop - Shutting down SelectorLoop blaze-acceptor-0-0 
[shutdownHook1] INFO  c.e.c.CatseffectbugServer - Shutting down app in 5 seconds 
[blaze-selector-1] INFO  o.h.s.m.Logger - HTTP/1.1 GET /hello/john Headers(User-Agent: PostmanRuntime/7.26.8, Accept: */*, Cache-Control: no-cache, Postman-Token: ceb5c801-c7c0-4491-868e-b082ca8e43a1, Host: localhost:8080, Accept-Encoding: gzip, deflate, br, Connection: keep-alive) body="" 
[blaze-selector-1] INFO  o.h.s.m.Logger - service canceled response for request 
[ioapp-compute-3] INFO  c.e.c.CatseffectbugServer - Goodbye! 
```


