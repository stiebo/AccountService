# SpringBoot Rest API Account Service (Java) project

## Background And Task
<p>Companies send out payrolls to employees using corporate mail. This solution has certain disadvantages related to security and usability. In this project, put on a robe of such an employee. As you're familiar with Java and Spring Framework, you've suggested an idea of sending payrolls to the employee's account on the corporate website. The management has approved your idea, but it will be you who will implement this project. You've decided to start by developing the API structure, then define the role model, implement the business logic, and, of course, ensure the security of the service.</p>

Link to the project: https://hyperskill.org/projects/217

Check out my profile: https://hyperskill.org/profile/500961738

# REST API Documentation
All documentation retrieved from https://hyperskill.org/projects/217, provided by JetBrains Academy.

Documentation provides an overview of the endpoints, role model, password requirements, security logging events, and other important information related to our REST API.

## Users and Authorization

Our service supports the following roles:

| Endpoint                   | Anonymous | User | Accountant | Administrator | Auditor |
|----------------------------|-----------|------|------------|---------------|---------|
| POST api/auth/signup       | +         | +    | +          | +             | +       |
| POST api/auth/changepass   | -         | +    | +          | +             | -       |
| GET api/empl/payment       | -         | +    | +          | -             | -       |
| POST api/acct/payments     | -         | -    | +          | -             | -       |
| PUT api/acct/payments      | -         | -    | +          | -             | -       |
| GET api/admin/user         | -         | -    | -          | +             | -       |
| DELETE api/admin/user      | -         | -    | -          | +             | -       |
| PUT api/admin/user/role    | -         | -    | -          | +             | -       |
| PUT api/admin/user/access  | -         | -    | -          | +             | -       |
| GET api/security/events    | -         | -    | -          | -             | +       |

The service requires Http Basic authentication for all endpoints except for signup.
Users can sign up themselves via *POST /api/auth/signup*. The Administrator is the user who registered first, all subsequent registrations automatically receive the User role. Other roles will be granted by the Administrator (see below).

## Endpoints

### Authentication

#### POST /api/auth/signup

Allows users to register on the service.

Request Body:
```json
{
   "name": "<String value, not empty>",
   "lastname": "<String value, not empty>",
   "email": "<String value, not empty>",
   "password": "<String value, see requirements below>"
}
```

Response (200 OK):
```json
{
  "id": "<Long value, not empty>",
  "name": "<String value, not empty>",
  "lastname": "<String value, not empty>",
  "email": "<String value, not empty>",
  "roles": "<[User roles]>"
}
```

#### POST /api/auth/changepass

Changes a user's password.

Request Body:
```json
{
   "currentPassword": "<String value>",
   "newPassword": "<String value, at least 12 characters>"
}
```

Response (200 OK):
```json
{
   "message": "Password changed successfully"
}
```

### Administrator

#### PUT /api/admin/user/role

Grant/Remove user roles (USER, ACCOUNTANT, AUDITOR).

Request Body:
```json
{
   "user": "<String value, not empty>",
   "role": "<User role>",
   "operation": "<[GRANT, REMOVE]>"
}
```
Response (200 OK):
```json
{
   "id": "<Long value, not empty>",   
   "name": "<String value, not empty>",
   "lastname": "<String value, not empty>",
   "email": "<String value, not empty>",
   "roles": "[<User roles>]"
}
```

#### DELETE /api/admin/user/{user email}

Delete a user.

Response (200 OK):
```json
{
   "user": "<user email>",
   "status": "Deleted successfully!"
}
```

#### GET /api/admin/user

Displays information about all users.

```json
[
    {
        "id": "<user1 id>",
        "name": "<user1 name>",
        "lastname": "<user1 last name>",
        "email": "<user1 email>",
        "roles": "<[user1 roles]>"
    },
    {
        "id": "<userN id>",
        "name": "<userN name>",
        "lastname": "<userN last name>",
        "email": "<userN email>",
        "roles": "<[userN roles]>"
    }
]
```

#### PUT /api/admin/user/access

Changes user access levels.

Request Body:
```json
{
   "user": "<String value, not empty>",
   "operation": "<[LOCK, UNLOCK]>" 
}
```
Response (200 OK):

```json
{
    "status": "User <username> <[locked, unlocked]>!"
}
```

### User

#### GET /api/empl/payment?period={mm-YYYY}

Gives access to the employee's payrolls. If the parameter period is not specified, the endpoint provides information about the employee's salary for each period from the database as an array of objects in descending order by date.

Response Body:
```json
{
   "name": "<user name>",
   "lastname": "<user lastname>",
   "period": "<name of month-YYYY>",
   "salary": "X dollar(s) Y cent(s)"
}
```

### Accountant

#### POST /api/acct/payments

Uploads payrolls.

Request Body:
```json
[
    {
        "employee": "<user email>",
        "period": "<mm-YYYY>",
        "salary": "<Long value, not empty>"
    },
    {
        "employee": "<userN email>",
        "period": "<mm-YYYY>",
        "salary": "<Long value not empty>"
    }
]
```

Response (200 OK):
```json
{
   "status": "Added successfully!"
}
```

#### PUT /api/acct/payments

Updates payment information.

Request Body:
```json
{
    "employee": "<user email>",
    "period": "<mm-YYYY>",
    "salary": "<Long value, not empty>"
}
```

Response (200 OK):
```json
{
   "status": "Updated successfully!"
}
```

### Security auditor

#### GET /api/security/events

Retrieves security events.

Response (200 OK):
```json
[
    {
        "date": "<date>",
        "action": "<event_name for event1>",
        "subject": "<The user who performed the action>",
        "object": "<The object on which the action was performed>",
        "path": "<api>"
    },
    {
        "date": "<date>",
        "action": "<event_name for eventN>",
        "subject": "<The user who performed the action>",
        "object": "<The object on which the action was performed>",
        "path": "<api>"
    }
]
```

## Password Requirements

- User passwords must contain at least 12 characters.
- Users can change their passwords. Changing the password requires the current and a new password.
- Passwords submitted during registration, login, and password change are checked against a set of breached passwords (a static list for testing purposes). If the password is breached, the application must require users to set a new non-breached password.
- Passwords are stored in a form that is resistant to offline attacks. Passwords must be salted and hashed using an approved one-way key derivation or a password hashing function.
- If using bcrypt, the work factor must be as large as the verification server performance will allow. Usually, at least 13.

## Logging Events

### Description

The service logs information security events, including:

| Description                                        | Event Name    |
|----------------------------------------------------|---------------|
| A user has been successfully registered            | CREATE_USER   |
| A user has changed the password successfully       | CHANGE_PASSWORD |
| A user is trying to access a resource without access rights | ACCESS_DENIED |
| Failed authentication                             | LOGIN_FAILED  |
| A role is granted to a user                       | GRANT_ROLE    |
| A role has been revoked                           | REMOVE_ROLE   |
| The Administrator has locked the user             | LOCK_USER     |
| The Administrator has unlocked a user             | UNLOCK_USER   |
| The Administrator has deleted a user              | DELETE_USER   |
| A user has been blocked on suspicion of a brute force attack | BRUTE_FORCE   |

#### Brute force attack
If there are more than 5 consecutive attempts to enter an incorrect password, an entry about this should appear in the security events. Also, the user account must be blocked.

## Data storage
The service includes an H2 file database for all data storage.

## Tests
Code tests were performed as part of the Hyperskill project with 100+ tests passed. Sample integration tests are only created here for limited test scenarios.