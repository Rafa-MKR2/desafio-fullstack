-- ============================================================
-- Cria o database do Keycloak (executa dentro do database "desafio")
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- ============================================================
-- init.sql - Dados seed do Desafio
-- 15 Disciplinas, 5 Professores, 9 Horários, 9 Cursos,
-- 3 Coordenadores, 5 Alunos
-- ============================================================

-- Schema (criado de forma idempotente, também gerido pelo Hibernate)
CREATE TABLE IF NOT EXISTS disciplina (
    id             BIGINT PRIMARY KEY,
    nome           VARCHAR(255) NOT NULL,
    carga_horaria  INTEGER
);

CREATE TABLE IF NOT EXISTS professor (
    id          BIGINT PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    disciplinas VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS horario (
    id           BIGINT PRIMARY KEY,
    dia_semana   VARCHAR(20),
    hora_inicio  VARCHAR(5),
    hora_fim     VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS curso (
    id   BIGINT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS coordenador (
    id       BIGINT PRIMARY KEY,
    nome     VARCHAR(255) NOT NULL,
    email    VARCHAR(255),
    area     VARCHAR(255),
    curso_id BIGINT
);

CREATE TABLE IF NOT EXISTS aluno (
    id        BIGINT PRIMARY KEY,
    nome      VARCHAR(255) NOT NULL,
    email     VARCHAR(255),
    curso_id  BIGINT,
    matricula VARCHAR(50)
);

-- Disciplinas (15)
INSERT INTO disciplina (id, nome, carga_horaria) VALUES
  (1,  'Matemática',          80),
  (2,  'Português',           80),
  (3,  'História',            60),
  (4,  'Geografia',           60),
  (5,  'Física',              60),
  (6,  'Química',             60),
  (7,  'Biologia',            60),
  (8,  'Inglês',              40),
  (9,  'Espanhol',            40),
  (10, 'Arte',                40),
  (11, 'Educação Física',     40),
  (12, 'Filosofia',           40),
  (13, 'Sociologia',          40),
  (14, 'Informática',         60),
  (15, 'Empreendedorismo',    40);

-- Professores (5)
INSERT INTO professor (id, nome, disciplinas) VALUES
  (1, 'Ana Paula',      'Matemática'),
  (2, 'Carlos Alberto', 'Português, História'),
  (3, 'Mariana Costa',  'Física, Química'),
  (4, 'Roberto Silva',  'Biologia, Geografia'),
  (5, 'Fernanda Lima',  'Inglês, Espanhol');

-- Horários (9)
INSERT INTO horario (id, dia_semana, hora_inicio, hora_fim) VALUES
  (1, 'Segunda', '08:00', '10:00'),
  (2, 'Segunda', '10:00', '12:00'),
  (3, 'Terça',   '08:00', '10:00'),
  (4, 'Terça',   '10:00', '12:00'),
  (5, 'Quarta',  '08:00', '10:00'),
  (6, 'Quarta',  '10:00', '12:00'),
  (7, 'Quinta',  '08:00', '10:00'),
  (8, 'Quinta',  '10:00', '12:00'),
  (9, 'Sexta',   '08:00', '10:00');

-- Cursos (9)
INSERT INTO curso (id, nome) VALUES
  (1, 'Engenharia Civil'),
  (2, 'Engenharia Elétrica'),
  (3, 'Engenharia Mecânica'),
  (4, 'Ciência da Computação'),
  (5, 'Administração'),
  (6, 'Direito'),
  (7, 'Medicina'),
  (8, 'Psicologia'),
  (9, 'Arquitetura');

-- Coordenadores (3) - relacionados aos usuários Keycloak
INSERT INTO coordenador (id, nome, email, area, curso_id) VALUES
  (1, 'Dr. José',       'coordenador1@email.com', 'Ciência da Computação',   4),
  (2, 'Profa. Maria',   'coordenador2@email.com', 'Engenharias',              1),
  (3, 'Dr. Paulo',      'coordenador3@email.com', 'Humanas',                  6);

-- Alunos (5) - relacionados aos usuários Keycloak
INSERT INTO aluno (id, nome, email, curso_id, matricula) VALUES
  (1, 'João Silva',     'aluno1@email.com', 4, '20240001'),
  (2, 'Maria Santos',   'aluno2@email.com', 7, '20240002'),
  (3, 'Pedro Costa',    'aluno3@email.com', 2, '20240003'),
  (4, 'Ana Oliveira',   'aluno4@email.com', 5, '20240004'),
  (5, 'Lucas Pereira',  'aluno5@email.com', 9, '20240005');