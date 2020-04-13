在提交issue之前，我们应该先查询是否已经有相关的issue和ReadMe中的注意事项，比如说Robust在0.3.3版本才支持windows开发。提交issue时，我们需要写明issue的原因，最好可以携带编译或运行过程的日志或者截图。issue最好以下面的格式提出：

异常类型：app运行时异常/编译异常

手机型号：如:Nexus 5(如是编译异常，则可以不填)

手机系统版本：如:Android 5.0 (如是编译异常，则可以不填)

Robust版本：如:0.4.99

Gradle版本：如:2.10

系统：如:Windows

堆栈/日志：

如是编译异常，请在执行gradle命令时，加上--stacktrace，并把结果重定向，例如在demo中重定向命令如下：./gradlew clean assembleRelease --stacktrace --no-daemon >log.txt ,结果重定向到当前的目录下的log.txt文件;
日志中我们需要过滤"robust"关键字，可以初步查找问题的大概原因;
Robust提供了sample样例与我们的源码，大家在使用前可以先将样例跑通，如遇任何疑问也欢迎大家提出，更鼓励大家给我们提pr，谢谢大家的支持.