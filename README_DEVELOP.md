## Develop Markdown

### For Windows:
1. install idea(2025.2.3 (Ultimate Edition))
2. install git( 2.51.0.windows.2)
3. install nodejs(v22.20.0), so as npm(11.6.1)
4. install yarn(1.22.22)
5. run
   ```powershell
   yarn
   ```
6. install java8 (openjdk8 Temurin1.8.0_462), so as javac(javac 1.8.0_462)
7. set java into SystemPath and Path, echo %JAVA_HOME% to verify
8. install maven and set into SystemPath and Path(Apache Maven 3.9.11)
10. run
   ```powershell
   mvn clean compile "-DskipTests" "-Dcheckstyle.skip=true" -pl common,spy,core,boot,agent
   ```
12. run
   ```powershell
   mvn compile "-DskipTests" "-Dcheckstyle.skip=true" -pl common,spy,core,agent,boot,math-game
   ```
12. run
   ```powershell
   mvn package "-DskipTests" "-Dcheckstyle.skip=true" -pl boot
   ```
13.
run in terminal 1:
   ```powershell
   curl -O https://arthas.aliyun.com/math-game.jar
   java -jar math-game.jar
   ```
run in terminal 2:
   ```powershell
   java -jar boot/target/arthas-boot-jar-with-dependencies.jar
   ```
Then success. have fun!

### For Linux:
1. update your dependencies by npm or yarn(suspicious)

   ```zsh
   npm install
   or
   yarn
   ```
2. make sure you have jdk, jre, nodejs, and the version is java8
3. make sure your maven use the correct JAVA_HOME, if not, try

   ```zsh
   JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn clean compile -DskipTests -Dcheckstyle.skip=true -pl common,spy,core,boot,agent
   ```

4. then let us start, you have two method to start the arthas

    - First, you can run the Class Bootstrap directly. The file is under path:
      ```zsh
      JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn clean compile -DskipTests -Dcheckstyle.skip=true -pl common,spy,core,boot,agent
      or
      mvn clean compile -DskipTests -Dcheckstyle.skip=true -pl common,spy,core,boot,agent
      ```

    - You can also build a jar, just by
      ```zsh
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

Then success. have fun!
