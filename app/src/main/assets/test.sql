PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    subscriptionId INTEGER NOT NULL,
    category INTEGER NOT NULL,
    FOREIGN KEY (subscriptionId) REFERENCES subscriptions (id) ON DELETE CASCADE
);
INSERT INTO categories VALUES(1,1,0);
INSERT INTO categories VALUES(2,2,1);
INSERT INTO categories VALUES(3,3,2);
INSERT INTO categories VALUES(4,4,3);
INSERT INTO categories VALUES(5,5,0);
INSERT INTO categories VALUES(6,6,0);
INSERT INTO categories VALUES(7,7,4);
INSERT INTO categories VALUES(8,8,5);
CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    subscriptionId INTEGER NOT NULL,
    shouldRemind INTEGER NOT NULL,
    reminderDate TEXT NOT NULL,
    daysBeforeToRemind INTEGER NOT NULL,
    showPriceChanges INTEGER NOT NULL,
    trialEndDate TEXT,
    FOREIGN KEY (subscriptionId) REFERENCES subscriptions (id) ON DELETE CASCADE
);
INSERT INTO notifications VALUES(1,1,1,'2026-10-18',3,1,NULL);
INSERT INTO notifications VALUES(2,2,1,'2026-10-12',3,1,NULL);
INSERT INTO notifications VALUES(3,3,1,'2027-09-18',3,1,NULL);
INSERT INTO notifications VALUES(4,4,0,'2026-10-07',3,0,NULL);
INSERT INTO notifications VALUES(5,5,1,'2026-10-22',3,1,NULL);
INSERT INTO notifications VALUES(6,6,1,'2026-10-02',3,1,'2024-11-05');
INSERT INTO notifications VALUES(7,7,1,'2026-10-15',3,1,NULL);
INSERT INTO notifications VALUES(8,8,1,'2026-10-09',3,0,NULL);
CREATE TABLE IF NOT EXISTS "subscriptions" (
	"id"	INTEGER NOT NULL,
	"name"	TEXT NOT NULL,
	"packageName"	TEXT NOT NULL,
	"price"	REAL NOT NULL,
	"billingPeriod"	TEXT NOT NULL DEFAULT 'MONTHLY',
	"nextRenewalDate"	TEXT NOT NULL,
	"status"	INTEGER NOT NULL DEFAULT 1,
	"startDate"	TEXT NOT NULL,
	"score"	INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT)
);
INSERT INTO subscriptions VALUES(1,'Netflix','com.netflix.mediaclient',15.99,'3 0','2026-10-21',1,'2024-10-21',8);
INSERT INTO subscriptions VALUES(2,'Spotify','com.spotify.music',10.99,'8 1','2026-10-15',1,'2024-10-15',9);
INSERT INTO subscriptions VALUES(3,'Microsoft 365','com.microsoft.office.officehub',99.99,'8 2','2027-09-21',1,'2024-09-21',7);
INSERT INTO subscriptions VALUES(4,'Adobe Creative Cloud','com.adobe.creativecloud',54.99,'6 0','2026-10-10',1,'2024-10-10',6);
INSERT INTO subscriptions VALUES(5,'Disney+','com.disney.disneyplus',7.99,'7 1','2026-10-25',1,'2024-10-25',8);
INSERT INTO subscriptions VALUES(6,'YouTube Premium','com.google.android.youtube',13.99,'6 2','2026-10-05',1,'2024-10-05',9);
INSERT INTO subscriptions VALUES(7,'Dropbox','com.dropbox.android',11.99,'7 0','2026-10-18',0,'2024-10-18',7);
INSERT INTO subscriptions VALUES(8,'Audible','com.audible.application',14.95,'1 1','2026-10-12',1,'2024-10-12',8);
PRAGMA writable_schema=ON;
CREATE TABLE IF NOT EXISTS sqlite_sequence(name,seq);
DELETE FROM sqlite_sequence;
INSERT INTO sqlite_sequence VALUES('categories',8);
INSERT INTO sqlite_sequence VALUES('notifications',8);
INSERT INTO sqlite_sequence VALUES('subscriptions',8);
CREATE INDEX index_categories_subscriptionId ON categories (subscriptionId);
CREATE INDEX index_notifications_subscriptionId ON notifications (subscriptionId);
CREATE INDEX index_subscriptions_packageName ON subscriptions (packageName);
PRAGMA writable_schema=OFF;
COMMIT;
