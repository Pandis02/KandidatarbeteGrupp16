## Technical Info

### Steps to run
1. Have vs code
3. Have the Java SDK (preferably 21)
3. Get `Extension Pack for Java`
4. Get `Spring Boot Extension Pack`
5. Click run button at the top right in vs code

### Directories
1. Model code (business logic) goes into `src/main/java/kg16/demo/model`
2. REST API controllers go into `src/main/java/kg16/demo/web`
    - Remember, there should be no business logic in here
    - It should be as if we can replace the web interface with a console one without much effort and without any changes in the model whatsoever
3. Frontend (HTML/CSS/JS) go into `src/main/resources/static`
4. DB Schema goes into `src/main/resources/schema.sql` but needs to be manually run in `h2-console` after a change

### Access the DB Admin panel
1. Run the application
2. Go to `localhost:8080/h2-console`
3. Paste `jdbc:h2:file:./store` into the JDBC url (only needed the first time)
4. Username is `sa` & there is no password
5. Click connect
6. You're free to execute any SQL you want
7. Also leave the `Users` table alone, thats for the DB itself and not ours