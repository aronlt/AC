##AC算法并行化的实现思路
采用JAVA实现了AC多模式匹配算法，并利用多线程的方式实现了算法的并行化匹配。<br\>
关于AC算法以及并行化的思路见doc文件夹下的ppt文件.<br\>


###每个类文件的说明
####pattern.java
该文件包含了模式串相关的类，其中PatternNode类用于表示一个模式串。PatternSet类用于表示模式串集合.<br\>
####state.java
该文件包含了状态相关的类，State类表示状态,以及状态相关的goto表，failure节点，以及output信息<br\>
####ac.java
AC多模式匹配算法,其中MyEntry类用于标示每个匹配的模式串相关信息，AC类是整个AC算法的核心，通过init方法构造<br\>
AC自动机，并通过match方法实现匹配。然后调用printResult方法输出结果<br\>
####test.java
测试AC算法，通过多线程的方式运行算法。并记录相关的时间信息
