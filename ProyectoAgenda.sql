-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS ProyectoAgenda;
USE ProyectoAgenda;

-- Eliminar tablas si existen
DROP TABLE IF EXISTS Notificacion;
DROP TABLE IF EXISTS Jefe_Tarea_Asignacion;
DROP TABLE IF EXISTS Evento;
DROP TABLE IF EXISTS Tarea;
DROP TABLE IF EXISTS Jefe;
DROP TABLE IF EXISTS Empleado;
DROP TABLE IF EXISTS Calendario;

-- Tabla Calendario
CREATE TABLE Calendario (
    idCalendario INT AUTO_INCREMENT PRIMARY KEY,
    nombreCalendario VARCHAR(255) NOT NULL,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla Empleado
CREATE TABLE Empleado (
    idEmpleado INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellidoPaterno VARCHAR(255) NOT NULL,
    apellidoMaterno VARCHAR(255) NOT NULL,
    edad INT NOT NULL,
    correoElectronico VARCHAR(255) NOT NULL,
    ocupacion VARCHAR(255) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    contraseña VARCHAR(255),
    PRIMARY KEY (idEmpleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla Jefe (referencia a Empleado)
CREATE TABLE Jefe (
    idJefe INT NOT NULL AUTO_INCREMENT,
    idEmpleado INT NOT NULL,
    contraseña VARCHAR(255) NOT NULL,
    PRIMARY KEY (idJefe),
    FOREIGN KEY (idEmpleado) REFERENCES Empleado(idEmpleado) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla Tarea (idTarea como CHAR)
CREATE TABLE Tarea (
    idTarea CHAR(36) NOT NULL,  -- Tipo CHAR para ID alfanumérico, por ejemplo, UUID
    nombreTarea VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    fechaInicio DATETIME NOT NULL,
    fechaFin DATETIME NOT NULL,
    encargado INT,  -- Referencia al empleado o jefe encargado de la tarea
    PRIMARY KEY (idTarea),
    FOREIGN KEY (encargado) REFERENCES Empleado(idEmpleado) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE Tarea MODIFY COLUMN encargado INT NULL;

-- Tabla Evento (idEvento como CHAR)
CREATE TABLE Evento (
    idEvento CHAR(36) PRIMARY KEY,
    nombreEvento VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fechaInicio DATE,
    horaInicio TIME,
    fechaFin DATE,
    horaFin TIME
);

-- Tabla Jefe_Tarea_Asignacion (relación entre Jefe, Tarea y Empleado)
CREATE TABLE Jefe_Tarea_Asignacion (
    idAsignacion INT NOT NULL AUTO_INCREMENT,
    idJefe INT NOT NULL,
    idTarea CHAR(36) NOT NULL,
    idEmpleado INT NOT NULL,
    PRIMARY KEY (idAsignacion),
    FOREIGN KEY (idJefe) REFERENCES Jefe(idJefe) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (idTarea) REFERENCES Tarea(idTarea) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (idEmpleado) REFERENCES Empleado(idEmpleado) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Notificacion (
    idNotificacion INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('evento', 'tarea', 'usuario', 'bienvenida') NOT NULL,
    idJefe INT NOT NULL,                      -- Jefe que asigna la tarea o evento
    idEmpleado INT,                           -- Empleado a quien se asigna la tarea (null si es evento)
    idEvento CHAR(36),                        -- Evento asociado (null si es tarea)
    idTarea CHAR(36),                         -- Tarea asociada (null si es evento)
    mensaje TEXT NOT NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idJefe) REFERENCES Jefe(idJefe),
    FOREIGN KEY (idEmpleado) REFERENCES Empleado(idEmpleado),
    FOREIGN KEY (idEvento) REFERENCES Evento(idEvento),
    FOREIGN KEY (idTarea) REFERENCES Tarea(idTarea)
);

ALTER TABLE Notificacion MODIFY COLUMN idJefe INT NULL;

-- Mostrar todos los registros de las tablas
SELECT * FROM Calendario;
SELECT * FROM Empleado;
SELECT * FROM Jefe;
SELECT * FROM Tarea;
SELECT * FROM Evento;
SELECT * FROM Jefe_Tarea_Asignacion;

-- Mostrar la estructura de cada tabla
DESCRIBE Calendario;
DESCRIBE Empleado;
DESCRIBE Jefe;
DESCRIBE Tarea;
DESCRIBE Evento;
DESCRIBE Jefe_Tarea_Asignacion;







