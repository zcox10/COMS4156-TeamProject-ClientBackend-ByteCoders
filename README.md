# COMS W4156 Advanced Software Engineering

## ByteCoders Group Project: EmergencyAid Client Backend

### Team Members:

- Navinashok Swaminathan - ns3886
- Oleksandr Loyko - ol2260
- Zach Cox - zsc2107
- Orli Elisabeth Cohen - oec2109

## Brief Description
This repo is the backend of the client app, it provides endpoints to register and login emergency responders. As well as supports functionality to register patients (Patients can be associated with their pharmaid during registration), view patients as well, view a patient's prescriptions using the pharmaID service.

## API Documentation

The API documentation is available through Swagger UI at:
https://emergencyaid-dot-bytecoders-coms4156.uk.r.appspot.com/swagger-ui/index.html#/


### Postman Collection
- Import the [Postman Collection](https://www.dropbox.com/scl/fi/rqvu6dfjy41hxam8v33vh/final_client_backend_api_test.postman_collection.json?rlkey=quikh7evv5nbvcz8ap7pf5b34&st=zdgfpf6p&dl=0) to test all endpoints. full link : https://www.dropbox.com/scl/fi/rqvu6dfjy41hxam8v33vh/final_client_backend_api_test.postman_collection.json?rlkey=quikh7evv5nbvcz8ap7pf5b34&st=zdgfpf6p&dl=0 .
- Also set an environment in postman to use {{base_url}} to be able to reuse variables when needed. value for base_url : https://emergencyaid-dot-bytecoders-coms4156.uk.r.appspot.com/ .


### Interaction Flow
1. Register a user account via POST {{base_url}}/register with email and password as body
```
     { 
        "email": "random_user@example.com",
        "password": "random_password"
     }
```
2. Login to receive a JWT token 
```
{
    "id": "485ae8c2-1687-4da5-b189-fc39935aa48a",
    "email": "random_user@example.com",
    "token": "eyJhbGciOiJIUzI1NiJ9.e..."
}
```
3. Use this token in the Authorization header for all subsequent requests:
```
   Authorization: Bearer jwt_token_from_login_response
```
4. All logged in users will be treated as emergency responders, who have view access to all patient records.
5. A logged in emergency responder can search for all patients using GET {{base_url}}/patients
6. Emergency responders can search for users using their first name, last name, phone numer or id using
GET {{base_url}}/patients/search?q=Patient_First_Name
7. Emergency responders can view a patient's presctiptions using  GET {{base_url}}/patients/{{patient_id}}/pharmaid/view






### Important Assets:

- [PharmaId Service - GitHub repo](https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders)
- [First Iteration Video Demo](https://www.dropbox.com/scl/fi/z5lcima7kjy1jpw5xwheb/Iteration-1-functionality-demo.mov?rlkey=ld4xxx19yk5ug3xetvunyk6yu&st=g5jnxxl7&dl=0)
- [First Iteration Postman Collection Logs](https://www.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&st=pjxn6cmh&dl=0)
- [JIRA Board](https://bytecoders-4156.atlassian.net/jira/software/projects/BYT/boards/1)
- [Postman collection](https://www.dropbox.com/scl/fi/rqvu6dfjy41hxam8v33vh/final_client_backend_api_test.postman_collection.json?rlkey=quikh7evv5nbvcz8ap7pf5b34&st=be7d7sql&dl=0)
- [Frontend repo](https://github.com/AlexLoyko/COMS4156-TeamProjectClient-ByteCoders/tree/main)