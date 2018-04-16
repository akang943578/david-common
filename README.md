## 公共类库
这是我的公共类库。公共类会提交到这里，供其他项目依赖。

### 使用方法：
#### 更新项目：
代码写好后，将其deploy到maven-repo项目里面。maven-repo项目push更新到github，项目重新依赖即可。
```
mvn clean deploy -DaltDeploymentRepository=jiakang-mvn-repo::default::file:/Users/jiakang/IdeaProjects/maven-repo/repository/ -Dmaven.test.skip=true
```
#### 初次创建项目：
简单来说，共有3步：
```
1.deploy到本地目录
2.把本地目录提交到gtihub上
3.配置github地址为仓库地址
```
##### 将其deploy到一个新项目目录下：
```
mvn clean deploy -DaltDeploymentRepository=jiakang-mvn-repo::default::file:/Users/jiakang/IdeaProjects/maven-repo/repository/ -Dmaven.test.skip=true
```
##### 把本地仓库提交到github上
上面把项目deploy到本地目录/Users/jiakang/IdeaProjects/maven-repo/repository/里，下面把这个目录提交到github上。
在Github上新建一个项目(maven-repo)，然后把/Users/jiakang/IdeaProjects/maven-repo/repository/下的文件都提交到gtihub上。
```
cd /Users/jiakang/IdeaProjects/maven-repo/
git init
git add repository/*
git commit -m 'first commit'
git remote add origin git@github.com:haojiakang/maven-repo.git
git push -u origin master
```
##### github maven仓库的使用
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