Spring Batch是用来处理大量数据操作的一个框架，主要用来读取大量数据，然后进行一定处理后输出到指定格式。
主要由以下几部分组成
|    名 称    |        用 途        |
|:------------|:--------------------|
|JobRepsitiory|用来注册Job容器       |
|JobLauncher  |用来启动Job接口       |
|Job          |我们要实际执行的任务，包含一个或多个Step|
|Step         |步骤包含ItemReader，ItemProcessor和ItemWriter|

