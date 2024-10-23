# Watch-2-Gather
An Application to watch movies together with your long distance friends and family. Watch together or Watch to Gather close!

![Project Screenshot](https://github.com/SpandanBasu/Watch-2-Gather/blob/master/watchtogether.jpeg?raw=true)

# Features:

1. One device plays video. And Other connected device starts playing at the same time
2. One device pauses the playback and other device also pauses
3. One Seeks to a time and other device video also seeks to that same exact time

# Highlights

Now you can connect with a call and watch movies with your loved ones. No matter how far you are nothing can prevent your movie time together

![Project Screenshot](https://github.com/SpandanBasu/Watch-2-Gather/blob/master/watchtogather2.png?raw=true)

# Functional Requirement Analysis

Watch2Gather Video Player

R1: Video Catalogue
Description (Scope) – Every video available on phone. Can directly play a video by tapping on it.
R1.1 Recycler Grid View by video thumbnail

R2: Browse from Folder
Description (Scope) – File manager will open and user will choose the video file
R2.1 Intent for File Manager

R3: Connect Section
Description (Scope) – User will connect the player with other user in this section
R3.1: Getting myself connected first: generate current user’s unique username which will uniquely identify this user.
        	R3.1.1 Signup:
        	Input: username (must be unique), new Password.
        	Output: Logged in, and Profile page
R3.1.2 Log in:
Input: username as previously chosen, Password
Output: Logged in, and Profile page

R4.1: Connect with others: Using someone’s username send connection request, and upon accepting connection start. Or manage previous connection.
	R4.1.1 Search bar for username
        	Input: username (must be unique)
        	Output: if valid “Request Connection”, else Invalid User message
R3.1.2 Request Send:
Input: Click send request
Output: Request sent. Connection Request Accepted (Connected), Connection Request Denied (Request Connection).

R3: Playback Section
Description (Scope) – Video Playback options
When ever an action is performed (Check if connected or not)
If Connected: Perform same action in both devices
Else Only on that device

