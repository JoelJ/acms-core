A very simple dependency injection library.

At the core of a microservice framework I'm creating as a learning exercise.

Setup
-----

 - Project uses Java 1.8 with the '-parameters' compiler option.
 - If using an IDE make sure you have set the '-parameters' compiler option on any project consuming this library
 - If using maven set the following

 ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.2</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <compilerArgument>-parameters</compilerArgument>
                </configuration>
            </plugin>
        </plugins>
    </build>
 ```
 - run `mvn install` and add the follow dependency to consume this library

 ```xml
    <dependency>
        <groupId>com.leftstache.acms</groupId>
        <artifactId>acms-core</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
 ```

