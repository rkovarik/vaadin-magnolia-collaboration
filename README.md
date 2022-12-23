# Basic Magnolia Editor

## Running the page editor application

1. Build/run the application
```bash
./mvnw
```
2. Open the application in your browser [http://localhost:8080/login](http://localhost:8080/login)
3. Log in with username/password (e.g mmonroe/mmonroe). The demo users are: _mmonroe, vvangogh, jbach, aschopenhauer, ldavinci, rdescartes, ppicasso, fvoltaire, aeinstein, ggalilei_.
4. Start editing a web page by selecting it from the left navigation menu. The URL should change to e.g. http://localhost:8080/?componentPath=%2Ftravel%2Fcontact
5. Select a page component by clicking a green bar inside the rendered page in the middle of the screen. The URL should change to e.g. http://localhost:8080/?dialog=travel-demo%3Acomponents%2FtextImage&componentPath=%2Ftravel%2Fcontact%2Fmain%2F00
6. Start editing a text in the form on the right side of the screen.
7. Do the same steps (2.-6.) with another user (e.g. name _aeinstein_, password _aeinstein_)
8. The collaboration engine notifies the user about the other user editing the same component.
9. As one of the users, save the edited text by pressing ENTER or clicking the save button at the bottom of the form.
10. The page is re-rendered with new data.
11. The chat at the bottom briefly informs other users which component was edited. 

> :warning: **The demo runs against https://demo.magnolia-cms.com/**: It might be broken by other users playing with it. But it should be restarted/reset every 30 minutes.

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
java -jar target/magnolia-page-editor-1.0-SNAPSHOT.jar
```

## Useful links

- Vaadin Collaboration Engine [https://vaadin.com/collaboration](https://vaadin.com/collaboration).
- Report issues, create pull requests in [GitHub](https://github.com/rkovarik/vaadin-magnolia-collaboration).
