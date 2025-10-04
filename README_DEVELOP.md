```markdown
## Develop Markdown

1. update your dependencies by npm or yarn(suspicious)

```bash
npm install
or
yarn
```

2. make sure you have jdk, jre, nodejs, and the version is java8
3. make sure your maven use the correct JAVA_HOME, if not, try

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn clean compile -DskipTests -Dcheckstyle.skip=true -pl common,spy,core,boot,agent
```

4. then let us start, you have two method to start the arthas

    - First, you can run the Class Bootstrap directly. The file is under path:
      ```bash
      JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn clean compile -DskipTests -Dcheckstyle.skip=true -pl common,spy,core,boot,agent
      ```

    - You can also build a jar, just by
      ```bash
      # 1. Make sure all modules are compiled
      mvn compile -DskipTests -Dcheckstyle.skip=true -pl common,spy,core,agent,boot,math-game
 
      # 2. Repackage the boot module
      mvn package -DskipTests -Dcheckstyle.skip=true -pl boot
 
      # One-click test (requires two terminals)
      # Terminal 1:
      curl -O https://arthas.aliyun.com/math-game.jar
      java -jar math-game.jar
 
      # Terminal 2:
      java -jar boot/target/arthas-boot-jar-with-dependencies.jar
      ```
```