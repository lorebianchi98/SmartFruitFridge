CREATE DATABASE IF NOT EXISTS `smart_fridge`;

USE `smart_fridge`;

DROP TABLE IF EXISTS `oxygen`;

CREATE TABLE `oxygen` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `oxygen` int(4) NOT NULL,
  `unit` varchar(4) NOT NULL,
  `emitter` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `temperature`;

CREATE TABLE `temperature` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `temperature` int(4) NOT NULL,
  `unit` varchar(4) NOT NULL,
  `chiller` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `ripening_notifier`;

CREATE TABLE `ripening_notifier` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ethylene_level` float(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;
