/* Put your final project reporting queries here */
USE cs_hu_310_final_project;

-- Calculate the GPA for student given a student_id (use student_id=1)
SELECT students.first_name, students.last_name, count(class_registrations.student_id) as number_of_classes,
sum(convert_to_grade_point(letter_grade)) as total_grade_points_earned,
sum(convert_to_grade_point(letter_grade)) / count(class_registrations.student_id) as GPA
FROM students
JOIN class_registrations
ON class_registrations.student_id = students.student_id
JOIN grades
ON grades.grade_id = class_registrations.grade_id
WHERE students.student_id = 1
GROUP BY students.student_id;

-- Calculate the GPA for each student (across all classes and all terms)
SELECT students.first_name, students.last_name, count(class_registrations.student_id) as number_of_classes,
sum(convert_to_grade_point(letter_grade)) as total_grade_points_earned,
sum(convert_to_grade_point(letter_grade)) / count(class_registrations.student_id) as GPA
FROM students
JOIN class_registrations
ON class_registrations.student_id = students.student_id
JOIN grades
ON grades.grade_id = class_registrations.grade_id
GROUP BY students.student_id;

-- Calculate the avg GPA for each class 
SELECT classes.code, classes.name, count(class_registrations.student_id) as number_of_grades,
sum(covert_to_grade(letter_grade)) as total_grade_points, 
count(class_registrations.student_id)/sum(covert_to_grade(letter_grade)) as "AVG GPA"
FROM classes
JOIN class_sections
ON class_sections.class_id = classes.class_id
JOIN class_registrations
ON class_registrations.class_section_id = classes_sections.class_section_id
JOIN grades
ON grades.grade_id = class_registrations.grade_id
GROUP BY classes.class_id;

-- Calculate the avg GPA for each class and term
SELECT classes.code, classes.name, terms.name as term, count(grade.letter_grade) as number_of_grades,
sum(covert_to_grade(letter_grade)) as total_grade_points, 
count(class_registrations.student_id)/sum(covert_to_grade(letter_grade)) as "AVG GPA"
FROM classes
LEFT JOIN class_sections
ON class_sections.class_id = classes.class_id
LEFT JOIN terms
ON terms.term_id = class_sections.term_id
LEFT JOIN class_registrations
ON class_registrations.class_section_id = classes_sections.class_section_id
LEFT JOIN grades
ON grades.grade_id = class_registrations.grade_id
GROUP BY classes.class_id;

-- List all the classes being taught by an instructor (use instructor_id=1) 
SELECT first_name, last_name, title, classes.code, classes.name as class_name, term FROM classes
JOIN
(SELECT class_id, instructor_id, term_id FROM class_sections
) as class
ON class.class_id = classes.class_id
JOIN
(SELECT insturctor_id, instructors.first_name, instructors.last_name, academic_title_id FROM instuctors
) as faculty
ON faculty.instructor_id = class.instructor_id
JOIN
(SELECT term_id as term FROM terms 
)as term_taught
ON term_taught.term_id = class.term_id
JOIN
(SELECT acadeimic_title_id, title as title FROM academic_titles
GROUP BY academic_title_id
) as prof_title
ON prof_title.academit_title_id = faculty.academic_title_id
WHERE instructor_id = 1;

-- List all classes with terms & instructor 
SELECT classes.code, classes.name, term, first_name, last_name FROM classes
JOIN
(SELECT class_id, instructor_id, term_id FROM class_sections
) as class
ON class.class_id = classes.class_id
JOIN
(SELECT insturctor_id, instructors.first_name, instructors.last_name, academic_title_id FROM instuctors
) as faculty
ON faculty.instructor_id = class.instructor_id
JOIN
(SELECT term_id as term FROM terms 
)as term_taught
ON term_taught.term_id = class.term_id;

-- Calculate the remaining space left in a class
SELECT classes.code, classes.name, term, enrolled_students, maximum_students - enrolled_students as space_remaining FROM classes
JOIN
(SELECT class_id, term_id, class_section_id FROM class_sections
GROUP BY class_section_id) as section
ON section.class_id = classes.class_id
JOIN
(SELECT term_id as term FROM terms 
)as term_taught
ON term_taught.term_id = section.term_id
JOIN
(SELECT class_section_id, count(student_id) as enrolled_students FROM class_registrations
GROUP BY class_section_id) as registered
ON registerd.class_section_id = section.class_section_id;
