### System Setup

##### Java version:

```
openjdk 11.0.17 2022-10-18 LTS  
OpenJDK Runtime Environment Corretto-11.0.17.8.1 (build 11.0.17+8-LTS)  
OpenJDK 64-Bit Server VM Corretto-11.0.17.8.1 (build 11.0.17+8-LTS, mixed mode)
```

##### Gradle version

```
Gradle 7.6
```

___

### Instructions

##### How to build the project

- Navigate to the LaundryBooking directory
- Clean and build the gradle project using the following command  
  `gradle clean && gradle build`
- This ^ will build the project and also run the automated tests written for the project

___

### Assumptions

- It's assumed that the **listBookedTimes** API is supposed to return all the booked times for all the laundry rooms and not booked times of the
  user/house
- The DataAccessor classes are No Op and it's assumed they will return the appropriate response by getting data from the datastore configured for the
  system

### SQL Database model

![](/Users/tuskaley/Documents/Design Diagrams/LaundryBooking.png)

