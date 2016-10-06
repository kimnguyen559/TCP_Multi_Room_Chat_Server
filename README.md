# TCP_Multi_Room_Chat_Server

This project implements a multiple-room chat Server that allows users to sign in a chosen room and exchange conversation with other users through instant messages.

By: Kim Nguyen - kimnguyen559@gmail.com

## User Stories

The following functionality is completed:

* User can connect to the Server through a TCP connection
* User can sign in and join one of two chat rooms, named “chat” and “hottub”
* User can send instant messages to all users in the same chat room
* User can quit the current chat room to join another one
* User can exit and close the connection

The following features are implemented:

* Establish the connection between Server and Client using Java Sorcket API
* Allow Server to serve multiple Client at the same time with Java Thread API
* Validate user name to make sure there is no duplicate
* Verify a user has been signed in before entering a chat room
* Announce a newcomer to existing users in the chat room
* Announce the departure of a user to other users
* Send appropriate error messages for bad commands

The following API is implemented:

* /rooms						: get list of chat rooms
* /join roomName				: join chat room 
* /leave						: leave chat room
* /quit						    : exit the system

## Video Walkthrough 

Here's a walkthrough of implemented user stories:

[Video Walkthrough](https://giphy.com/gifs/3oz8xsl53QFq1dJRVm?status=200/)





