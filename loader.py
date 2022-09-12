import sqlite3
import json
import uuid

SQL_CREATE = '''
CREATE TABLE "player_name" (
	"index"	INTEGER NOT NULL UNIQUE,
	"uuid"	BLOB NOT NULL,
	"name"	TEXT NOT NULL,
	"changedToAt"	INTEGER NOT NULL DEFAULT 0,
	"source"	INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY("index" AUTOINCREMENT)
)
'''

SQL_DELETE = '''
DELETE FROM "player_name"
WHERE 1=1
'''

SQL_INSERT_1 = '''
INSERT INTO "player_name" ("uuid","name","changedToAt","source")
VALUES (?,?,?,?)
'''

SQL_INSERT_2 = '''
INSERT INTO "player_name" ("uuid","name","source")
VALUES (?,?,?)
'''

def main(args: list[str]):
    dbfile = args[0]
    ifile = args[1]
    records = list()
    with open(ifile) as fp:
        data = json.load(fp)
        for d in data:
            _uuid = uuid.UUID(d['uuid'])
            for namet in d['names']:
                _name = namet['name']
                _changedToAt = namet.get('changedToAt')
                records.append((_uuid, _name, _changedToAt, 0))
    records.sort(key=lambda t: 0 if t[2] is None else t[2])
    
    conn = sqlite3.connect(dbfile)
    try:
        conn.execute(SQL_CREATE)
    except sqlite3.DatabaseError as e:
        print(e)
        conn.execute(SQL_DELETE)
        pass

    i = 0
    n = len(records)
    c = conn.cursor()
    for (_uuid, _name, _changedToAt, _source) in records:
        if _changedToAt is None:
            t = (_uuid.bytes, _name, _source)
            c.execute(SQL_INSERT_2, t)
        else:
            t = (_uuid.bytes, _name, _changedToAt, _source)
            c.execute(SQL_INSERT_1, t)
        i += 1
        if i % 20 == 0:
            print(i * 100 / n, '%')
            conn.commit()
    
    conn.close()
    pass

if __name__ == '__main__':
    import sys
    args = sys.argv[1:]
    if len(args) < 2:
        dbfile = input('database: ')
        ifile = input('input: ')
        args = [dbfile, ifile]
    main(args)