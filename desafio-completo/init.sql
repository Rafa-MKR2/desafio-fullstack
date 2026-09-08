-- ============================================================
-- Cria os databases auxiliares (executa dentro do database "desafio")
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

SELECT 'CREATE DATABASE desafio_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'desafio_test')\gexec

-- ============================================================
-- init.sql - Dados seed do Desafio
-- 15 Disciplinas, 5 Professores, 9 Horários, 9 Cursos,
-- 3 Coordenadores, 5 Alunos
-- ============================================================

-- Schema (criado de forma idempotente, também gerido pelo Hibernate)
CREATE TABLE IF NOT EXISTS disciplina (
    id             BIGSERIAL PRIMARY KEY,
    nome           VARCHAR(255) NOT NULL,
    carga_horaria  INTEGER
);

CREATE TABLE IF NOT EXISTS professor (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS professor_disciplina (
    professor_id  BIGINT NOT NULL REFERENCES professor(id),
    disciplina_id BIGINT NOT NULL REFERENCES disciplina(id),
    PRIMARY KEY (professor_id, disciplina_id)
);

CREATE TABLE IF NOT EXISTS horario (
    id           BIGSERIAL PRIMARY KEY,
    dia_semana   VARCHAR(20),
    hora_inicio  VARCHAR(5),
    hora_fim     VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS curso (
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS coordenador (
    id       BIGSERIAL PRIMARY KEY,
    nome     VARCHAR(255) NOT NULL,
    email    VARCHAR(255),
    area     VARCHAR(255),
    curso_id BIGINT REFERENCES curso(id)
);

CREATE TABLE IF NOT EXISTS aluno (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(255) NOT NULL,
    email     VARCHAR(255),
    curso_id  BIGINT REFERENCES curso(id),
    matricula VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS aula (
    id            BIGSERIAL PRIMARY KEY,
    disciplina_id BIGINT NOT NULL REFERENCES disciplina(id),
    professor_id  BIGINT NOT NULL REFERENCES professor(id),
    horario_id    BIGINT NOT NULL REFERENCES horario(id),
    coordenador_id BIGINT NOT NULL REFERENCES coordenador(id),
    vagas         INTEGER NOT NULL,
    ativo         BOOLEAN NOT NULL DEFAULT TRUE,
    version       BIGINT NOT NULL DEFAULT 0
);

-- Cursos autorizados a se matricular na aula (N:N)
CREATE TABLE IF NOT EXISTS aula_curso (
    aula_id  BIGINT NOT NULL REFERENCES aula(id),
    curso_id BIGINT NOT NULL REFERENCES curso(id),
    PRIMARY KEY (aula_id, curso_id)
);

CREATE TABLE IF NOT EXISTS matricula (
    id       BIGSERIAL PRIMARY KEY,
    aluno_id BIGINT NOT NULL REFERENCES aluno(id),
    aula_id  BIGINT NOT NULL REFERENCES aula(id),
    CONSTRAINT matricula_aluno_aula_unique UNIQUE (aluno_id, aula_id)
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

-- Professores (8)
INSERT INTO professor (id, nome) VALUES
  (1, 'Ana Paula'),
  (2, 'Carlos Alberto'),
  (3, 'Mariana Costa'),
  (4, 'Roberto Silva'),
  (5, 'Fernanda Lima'),
  (6, 'Juliana Mendes'),
  (7, 'Paulo Henrique'),
  (8, 'Ricardo Nunes');

-- Relacionamento Professor <-> Disciplina
INSERT INTO professor_disciplina (professor_id, disciplina_id) VALUES
  (1, 1),
  (2, 2), (2, 3),
  (3, 5), (3, 6),
  (4, 7), (4, 4),
  (5, 8), (5, 9),
  (6, 10), (6, 11),
  (7, 12), (7, 13),
  (8, 14), (8, 15);

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

-- Restaura sequências após inserir IDs explícitos
SELECT setval(pg_get_serial_sequence('disciplina', 'id'), (SELECT MAX(id) FROM disciplina));
SELECT setval(pg_get_serial_sequence('professor', 'id'), (SELECT MAX(id) FROM professor));
SELECT setval(pg_get_serial_sequence('horario', 'id'), (SELECT MAX(id) FROM horario));
SELECT setval(pg_get_serial_sequence('curso', 'id'), (SELECT MAX(id) FROM curso));
SELECT setval(pg_get_serial_sequence('coordenador', 'id'), (SELECT MAX(id) FROM coordenador));
SELECT setval(pg_get_serial_sequence('aluno', 'id'), (SELECT MAX(id) FROM aluno));