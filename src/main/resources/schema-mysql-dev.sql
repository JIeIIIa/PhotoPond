DROP TABLE IF EXISTS picturefiles;
DROP TABLE IF EXISTS directories;
DROP TABLE IF EXISTS usersinfo;

CREATE TABLE IF NOT EXISTS usersinfo
(
id       BIGINT AUTO_INCREMENT
PRIMARY KEY,
login    VARCHAR(30) NOT NULL,
password VARCHAR(30) NOT NULL,
role     INT         NULL
);


CREATE TABLE IF NOT EXISTS directories
(
id       BIGINT AUTO_INCREMENT
PRIMARY KEY,
level    INT          NULL,
path     VARCHAR(255) NOT NULL,
owner_id BIGINT       NOT NULL,
CONSTRAINT FKaim5wp63d8bepmmqypby5m95k
FOREIGN KEY (owner_id) REFERENCES usersinfo (id)
ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX FKaim5wp63d8bepmmqypby5m95k
  ON directories (owner_id);




CREATE TABLE IF NOT EXISTS picturefiles
(
id           BIGINT AUTO_INCREMENT
PRIMARY KEY,
filename     VARCHAR(255) NOT NULL,
directory_id BIGINT       NOT NULL,
CONSTRAINT FK4ao6u9ungpkh3rtbxsu5rwj0t
FOREIGN KEY (directory_id) REFERENCES directories (id)
ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX FK4ao6u9ungpkh3rtbxsu5rwj0t
  ON picturefiles (directory_id) ;