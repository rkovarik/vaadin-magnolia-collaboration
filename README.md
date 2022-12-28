# Basic Magnolia Editor

## Running the page editor application

1. Build the application
```bash
./mvnw
```
2. Run/test the application
```bash
cd demo
.././mvnw spring-boot:run
```
3. Open the application in your browser [http://localhost:8080/login](http://localhost:8080/login)
4. Log in with username/password (e.g mmonroe/mmonroe). The demo users are: _mmonroe, vvangogh, jbach, aschopenhauer, ldavinci, rdescartes, ppicasso, fvoltaire, aeinstein, ggalilei_.
5. Start editing a web page by selecting it from the left navigation menu. The URL should change to e.g. http://localhost:8080/?componentPath=%2Ftravel%2Fcontact
6. Editable components are marked by pencil icon on the right side of the green bars. Edit a page component by clicking the edit icon inside the rendered page in the middle of the screen. The URL should change to e.g. http://localhost:8080/?dialog=textImage&componentPath=%2Ftravel%2Fcontact%2Fmain%2F00
7. Start editing a text in the form on the right side of the screen.
8. Do the same steps (2.-6.) with another user (e.g. name _aeinstein_, password _aeinstein_)
9. The collaboration engine notifies the user about the other user editing the same component.
10. As one of the users, save the edited text by clicking the save button at the bottom of the form.
11.The page is re-rendered with new data.
12. The chat at the bottom briefly informs other users which component was edited. 

> :warning: **The demo runs against https://demo.magnolia-cms.com/**: It might be broken by other users playing with it and you might get an error like _Can't fetch the Magnolia page /travel_. But it should be restarted/reset every 30 minutes. 

## Developing the page editor application

You can import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different 
IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
```bash
./mvnw clean package -Pproduction
```
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.
Once the JAR file is built, you can run it using
```bash
cd demo
java -jar target/magnolia-page-editor-demo-1.0-SNAPSHOT.jar
```

## Useful links

- Vaadin Collaboration Engine [https://vaadin.com/collaboration](https://vaadin.com/collaboration).
- Report issues, create pull requests in [GitHub](https://github.com/rkovarik/vaadin-magnolia-collaboration).
