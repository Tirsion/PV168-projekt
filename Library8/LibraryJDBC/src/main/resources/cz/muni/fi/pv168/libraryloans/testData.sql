INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHED, NOTE) VALUES ('Syntagma musicum','Michael Praetorius',1620,'facsimile');

INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHED) VALUES ('Hudba stredoveku','Richard H. Hoppin',2007);

INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHED) VALUES ('Hudba věku melancholie','Roman Dykast',2005);

INSERT INTO READER (NAME, ADDRESS, EMAIL) VALUES ('Henrich Glareanus','Basel, Hauptplatz 4', 'h.l.glareanus@musicaantiqua.com');

INSERT INTO READER (NAME, ADDRESS, EMAIL) VALUES ('Roland de Lassus','München, Burg 1', 'r.lassus@musicaantiqua.com');

INSERT INTO READER (NAME, ADDRESS, EMAIL) VALUES ('Jacob Gallus','Olomouc, Biskupské Náměstí 1', 'j.h.gallus@musicaantiqua.com');

INSERT INTO LOAN (READERID, BOOKID, STARTTIME, ENDTIME) VALUES (2,1,'2015-08-12','2015-12-12');

INSERT INTO LOAN (READERID, BOOKID, STARTTIME, ENDTIME, REALEND) VALUES (3,3,'2015-08-12','2015-12-12', '2015-11-22 14:04:41');