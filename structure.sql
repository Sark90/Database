/*
CREATE DATABASE "TESTING"
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Russian_Russia.1251'
    LC_CTYPE = 'Russian_Russia.1251'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
COMMENT ON DATABASE "TESTING" IS 'lesson 24';

--COMMIT WORK;

CONNECT 'TESTING' USER 'postgres' PASSWORD 'admin';
SET AUTODDL ON;
*/
CREATE TABLE public.themes
(
    id serial NOT NULL,
    theme varchar(50) NOT NULL,
    CONSTRAINT themes_pkey PRIMARY KEY (id)
)
WITH (OIDS = FALSE)
TABLESPACE pg_default;
ALTER TABLE public.themes OWNER to postgres;
--COMMIT WORK;

CREATE TABLE public.questions
(
    id serial,
    theme_id integer NOT NULL,
    question varchar(100) COLLATE pg_catalog."default" NOT NULL,    
    CONSTRAINT questions_pkey PRIMARY KEY (id),
    CONSTRAINT questions_theme_id_fkey FOREIGN KEY (theme_id)
        REFERENCES public.themes (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
WITH (OIDS = FALSE)
TABLESPACE pg_default;
ALTER TABLE public.questions OWNER to postgres;
--COMMIT WORK;

CREATE TABLE public.answers
(
    id serial,
    question_id integer NOT NULL,
    variant character(1) COLLATE pg_catalog."default" NOT NULL,
    answer varchar(100) COLLATE pg_catalog."default" NOT NULL,
    is_right boolean,
    CONSTRAINT answers_pkey PRIMARY KEY (id),
    CONSTRAINT answers_question_id_fkey FOREIGN KEY (question_id)
        REFERENCES public.questions (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
WITH (OIDS = FALSE)
TABLESPACE pg_default;
ALTER TABLE public.answers OWNER to postgres;
--COMMIT WORK;

CREATE TABLE public.students
(
    id serial,
    f_name varchar(50) COLLATE pg_catalog."default" NOT NULL,
    l_name varchar(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT students_pkey PRIMARY KEY (id)
)
WITH (OIDS = FALSE)
TABLESPACE pg_default;
ALTER TABLE public.students OWNER to postgres;
--COMMIT WORK;

CREATE TABLE public.tests
(
    id serial,
	test_id integer NOT NULL,
    student_id integer NOT NULL,
    theme_id integer NOT NULL,
    question_id integer NOT NULL,
    answer_id integer NOT NULL,    
    CONSTRAINT tests_pkey PRIMARY KEY (id),
    CONSTRAINT tests_answer_id_fkey FOREIGN KEY (answer_id)
        REFERENCES public.answers (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT tests_question_id_fkey FOREIGN KEY (question_id)
        REFERENCES public.questions (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT tests_student_id_fkey FOREIGN KEY (student_id)
        REFERENCES public.students (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT tests_theme_id_fkey FOREIGN KEY (theme_id)
        REFERENCES public.themes (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
WITH (OIDS = FALSE)
TABLESPACE pg_default;
ALTER TABLE public.tests OWNER to postgres;
--COMMIT WORK;