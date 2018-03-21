'use strict'

const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

var db = admin.database();

var body;

exports.deleteExpired  = functions.database.ref('/deleteExpired/{user_id}')
	.onWrite(event =>
	{
		console.log('deleteExpired function called!');

		if(!event.data.val())
		{
			return console.log('Key deleted!');
		}

		var tripEntryRef = db.ref("entries");
		var pairUpRef = db.ref("pairUps");
		var userRef = db.ref("users");

		tripEntryRef.once('value', function(entrySnapshot)
		{
  		entrySnapshot.forEach(function(entryChildSnapshot)
			{
    		var childKey = entryChildSnapshot.key;
				console.log(`key = ${childKey}`);

				var childData = entryChildSnapshot.val();
				var date = childData.date;
				var time = childData.time;

				var dateParts = date.split("/");
				var timeParts = time.split(":");
				var dateObject = new Date(dateParts[2], dateParts[1] - 1, dateParts[0], timeParts[0], timeParts[1], 0); // month is 0-based

				var currentDate = new Date();
				var currentOffset = currentDate.getTimezoneOffset();
				var ISTOffset = 330;   // IST offset UTC +5:30
				var ISTTime = new Date(currentDate.getTime() + (ISTOffset + currentOffset)*60000);

				console.log(`${ISTTime}`);
				console.log(`${dateObject}`);

				if(ISTTime >= dateObject)
					{
						console.log('DELETE!');
						tripEntryRef.child(childKey).remove();

						//remove tripEntry from user
						var userEntryRef = db.ref(`users/${childData.user_id}/userTripEntries`);
						userEntryRef.once('value', function(userEntrySnap)
						{
							userEntrySnap.forEach(function(userEntryChildSnap)
							{
								if(userEntryChildSnap.val().entry_id === childKey)
								{
									console.log(`DELETE USERENTRY ${childKey}`);
									userEntryRef.child(userEntryChildSnap.key).remove();
								}
							});
						});

						//main date checking if ends
					}

					//tripEntry forEach ends
			});

			//tripEntry once ends
		});

		//remove pairUps
		pairUpRef.once('value', function(pairUpSnapshot)
		{
			pairUpSnapshot.forEach(function(pairUpChildSnapshot)
			{
				var pairUpChildData = pairUpChildSnapshot.val();

				var puKey = pairUpChildData.pairUpId;
				var puDate = pairUpChildData.expiryDate;
				console.log(`puDate is ${puDate}`);
				var puDateParts = puDate.split("/");

				var puDateObj = new Date(puDateParts[2], puDateParts[1] - 1, puDateParts[0], (puDateParts[3] + 4)%24, puDateParts[4], 0); //give them 4 hrs

				if(ISTTime >= puDateObj)
				{
					console.log(`DELETE PAIRUP ${puKey}`);

					var creatorId = pairUpChildData.creatorId;
					var requesterId = pairUpChildData.requesterId;

					pairUpRef.child(puKey).remove();

					//reomove pairUp from both users
					//removing from creator
					var creatorPuRef = db.ref(`users/${creatorId}/pairUps`);
					creatorPuRef.once('value', function(creatorPuSnap)
					{
						creatorPuSnap.forEach(function(creatorPuChildSnap)
						{
							if(creatorPuChildSnap.val().pairUpId === puKey)
							{
								console.log(`${puKey} to be removed from ${creatorId}`);
								creatorPuRef.child(creatorPuChildSnap.key).remove();
							}
						});
					});

					//reomove pairUp from both users
					//reomving from requester
					var requesterPuRef = db.ref(`users/${requesterId}/pairUps`);
					requesterPuRef.once('value', function(requesterPuSnap)
					{
						requesterPuSnap.forEach(function(requesterPuChildSnap)
						{
							if(requesterPuChildSnap.val().pairUpId === puKey)
							{
								console.log(`${puKey} to be removed from ${requesterId}`);
								requesterPuRef.child(requesterPuChildSnap.key).remove();
							}
						});
					});

					//remove messages from both users
					//removing from creator
					var creatorMessageRef = db.ref(`messages/${creatorId}`);
					creatorMessageRef.once('value', function(creatorMessageSnap)
					{
						creatorMessageSnap.forEach(function(creatorMessageChildSnap)
						{
							if(creatorMessageChildSnap.val().pairUpId === puKey)
							{
								creatorMessageRef.child(creatorMessageChildSnap.key).remove();
							}
						});
					});

					//remove messages from both users
					//removing from requester
					var requesterMessageRef = db.ref(`messages/${requesterId}`);
					requesterMessageRef.once('value', function(requesterMessageSnap)
					{
						requesterMessageSnap.forEach(function(requesterMessageChildSnap)
						{
							if(requesterMessageChildSnap.val().pairUpId === puKey)
							{
								requesterMessageRef.child(requesterMessageChildSnap.key).remove();
							}
						});
					});

					//pairUp if ends
				}

				//pairUp forEach ends
			});

			//pairUp once ends
		});

		return 1;
	});

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}')
	.onWrite(event =>
	{
		const user_id = event.params.user_id;
		const notification_id = event.params.notification_id;

		console.log('We have a notif to send to : ', user_id);

		if(!event.data.val())
		{
			return console.log('A notif has been deleted from the DB : ', notification_id);
		}

		const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
		return fromUser.then(fromUserResult =>
		{
			const from_user_id = fromUserResult.val().from;
			var notif_type = fromUserResult.val().type;

			console.log('New notif from : ', from_user_id);
			console.log('Notification of type : ', notif_type);

			const userQuery = admin.database().ref(`users/${from_user_id}/name`).once('value');
			return userQuery.then(userResult =>
			{
				const userName = userResult.val();

				switch (notif_type)
				{
					case "requestAccepted":
						body = `${userName} has accepted your request`;
						console.log('Sure is!');
						break;

					case "requestCreated":
						body = `${userName} has sent you a request`;
						console.log('Sure is!');
						break;

					case "chat":
						body = `New messages from ${userName}`;
						console.log('Chat notif');
						break;

					default: break;
				}

				const deviceToken = admin.database().ref(`/users/${user_id}/deviceToken`).once('value');

				return deviceToken.then(result =>
				{
					const token_id = result.val();
					console.log(`Device token is ${token_id}`);

					const payload =
					{
						notification :
						{
							title : "UniPool",
							body : `${body}`,
							icon : "default",
							sound : "default",
							click_action : "android.intent.action.TARGET_NOTIFICATION"
						}
					};

					return admin.messaging().sendToDevice(token_id, payload).then(response =>
					{
						console.log('This was the notifications feature!');

						const error = response.error;
      			if (error)
						{
							console.error('Failure sending notification to', token_id, error);
						}

						return 1;
					});

				});

			});

		});

	});
