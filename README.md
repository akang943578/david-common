## 公共类库
这是我的公共类库。公共类会提交到这里，供其他项目依赖。

### 使用方法：
#### 1.deploy到本地maven-repo项目
代码写好后，将其deploy到maven-repo项目目录，/Users/jiakang/IdeaProjects/maven-repo/repository/。
```
mvn clean deploy -DaltDeploymentRepository=jiakang-mvn-repo::default::file:/Users/jiakang/IdeaProjects/maven-repo/repository/ -Dmaven.test.skip=true
```
#### 2.本地maven-repo项目push到github
* 进入/Users/jiakang/IdeaProjects/maven-repo/，如果已经在github创建了maven-repo项目，直接add,commit,push即可。
* 如果没有创建maven-repo项目，需要在Github上新建一个项目(maven-repo)，然后把/Users/jiakang/IdeaProjects/maven-repo/repository/下的文件都提交到gtihub上：
```
cd /Users/jiakang/IdeaProjects/maven-repo/
git init
git add repository/*
git commit -m 'first commit'
git remote add origin git@github.com:haojiakang/maven-repo.git
git push -u origin master
```
#### 3.为需要的项目添加maven依赖
因为github使用了raw.githubusercontent.com这个域名用于raw文件下载。所以使用这个maven仓库，只要在pom.xml里增加：
```
<repositories>
    <repository>
        <id>jiakang-maven-repo</id>
        <url>https://raw.githubusercontent.com/haojiakang/maven-repo/master/repository</url>
    </repository>
</repositories>
```
然后依赖所需要的包即可：
```
<dependency>
    <groupId>com.david.common</groupId>
    <artifactId>common-code</artifactId>
    <version>${common.version}</version>
</dependency>
```
### 批量设置所有module的pom版本
```
mvn --batch-mode release:update-versions -DdevelopmentVersion=${common.version}
```

### 注意：
如果maven所使用的settings.xml里面配置了镜像服务器，并且mirrorOf为*，则一定将jiakang-maven-repo排除，否则将无法找到github仓库地址，下载不到依赖。
如果有如下配置：
```
 <mirrors>
    <mirror>
     <id>weibo</id>
     <mirrorOf>*</mirrorOf>
     <name>weibo maven nexus mirror</name>
     <url>http://maven.intra.weibo.com/nexus/content/groups/public</url>
   </mirror>
 </mirrors>
```
一定将其改成：
```
 <mirrors>
    <mirror>
     <id>weibo</id>
     <mirrorOf>*,!jiakang-maven-repo</mirrorOf>
     <name>weibo maven nexus mirror</name>
     <url>http://maven.intra.weibo.com/nexus/content/groups/public</url>
   </mirror>
 </mirrors>
```
注意改的是mirrorOf，切记！！！