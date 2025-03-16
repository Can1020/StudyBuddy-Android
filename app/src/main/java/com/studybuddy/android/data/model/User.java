package com.studybuddy.android.data.model;

public class User {
    private String id;
    private String name;
    private int age;
    private String location;
    private String university;
    private String courseOfStudy;
    private String semester;
    private String skills;

    public User() {}


    public User(String id, String name, int age, String location,  String university, String course, String semester, String skills) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.location = location;
        this.university = university;
        this.courseOfStudy = course;
        this.semester = semester;
        this.skills = skills;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getLocation() { return location; }
    public String getUniversity() { return university; }
    public String getCourseOfStudy() { return courseOfStudy; }
    public String getSemester() { return semester; }
    public String getSkills() { return skills; }
}
