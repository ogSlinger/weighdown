# Weigh Down
## Derek Spaulding
## Southern New Hampshire University
## CS 360 Project
## October 21, 2024
## Time to complete: ~ 4 Working Days


![Alt text](https://i.imgur.com/86HYIvA.png)

- ### Briefly summarize the requirements and goals of the app you developed. What user needs was this app designed to address?
  - The assignment provided was a choice between three applications to develop for the course. The one I chose to develop was the weight tracking application. The assignment given was as follows.
>  This application will be used to track the daily weight of the user. This application must include the following features:  
>  - A database with at least three tables: one to store the daily weight, one to store user logins and passwords, and one to store the goal weight  
>  Note that goal weight will be constant but setting a one-time weight that is stored in a database will be the simplest way for you to accomplish this task.  
>  - A screen for logging into the app  
>  - Note that the screen for logging into the app should also be used to create a login if the user has never logged in before.  
>  - A screen with a grid that displays all the daily weights and the days they were entered  
>  - A mechanism by which the user can add a daily weight  
>  - A mechanism by which the user can add a goal weight  
>  - A mechanism by which the application will notify the user when they reach their goal weight  


  - The needs of the user is to have a way to record and get alerted when they achieved their goal weight.


- ### What screens and features were necessary to support user needs and produce a user-centered UI for the app? How did your UI designs keep users in mind? Why were your designs successful?
  - I utilized two fragments for my application. One of which held the login fragment, and the other was essentially the "main" fragement. After a successful login, the user would be brought to main fragment where a table would be populated and the user is shown their previously stored weights, the goal weight would maintain the recent goal weight, and all the actions that can be done to the record is found on that one fragment. This allows for the user to have an experience that doesn't require to jump around different screens, which may be a jarring experience. Any prompts for inputs were handled via alert dialogues. I populated the table dynamically with alternating shaded colors that makes the data more readable. The color theme is also chosen with complementary colors in mind. The overall application I designed also with as general purpose verbage to allow for extended use beyond user weight tracking, but tracking of any weighted object.
 

- How did you approach the process of coding your app? What techniques or strategies did you use? How could those techniques or strategies be applied in the future?
  - I utilized claude ai to research into the different views and fragments that could help me along the way. The reading material was very robust and it was hard to commit to memory. So, I made sure to develop incrementally with the core functionality being simple for each part of the application. So, after designing the UI, creating the initial functionality was getting a FragmentContainer to swap between fragments on successful login via a button listener. There was a requirement to check a user's username and password, so for storing the credentials I rinsed the password through SHA-256 hashing algorithm so no user's passwords were stored in plain text. Then, the table layout had to be created and populated dynamically with each entry of the database. This one required a bit more finesse, as I had to make sure the Cursor never went out of bounds as I iterated backwards through the database. This allowed the most recent addition to be put to the top. The next part was getting buttons to populate between each of the entries when a user hits "Edit Data". These buttons were assigned the indices inverse to the entries given to ensure they pointed to the right entry in the database.


- ### How did you test to ensure your code was functional? Why is this process important, and what did it reveal?
  - I ensured that whenever I got done developing a component of the system to test it via the Android Studio emulator. This was done in an incremental process per component. There was a particular moment where the app as crashing due to the Cursor object being indexed as -1, and that was found out via Logcat and echoing it's data at multiple stages of populating the table. This taught me to properly close a cursor object and just create a new one when needed.


- ### Consider the full app design and development process from initial planning to finalization. Where did you have to innovate to overcome a challenge?
  - I think the major thing I innovated with was utilizing a shaded rounded drawable as a means to highlight regions of the app and group different UI components together. I grouped things like the logo and title, the weight table title and weight table entries, and I also shaded each individual weight entry as an alternating color. I felt this made the app very visually pleasing and allowed for better readability of the data.
 

- In what specific component of your mobile app were you particularly successful in demonstrating your knowledge, skills, and experience?
  - I feel the best part of demonstrating my skills was the function "populateWeightTable" that houses a great deal of the logic of the application. From getting the database entries, creating the text view objects and populating them into the table, shading each entry with different colors, applying justified formatting, the creation of edit buttons, and using the logic of manipulating the data in the database I felt was a great exercise for myself.
