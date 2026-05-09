package me.johnra.tutorial.skills;

public interface Skill<T> {
    String skillName();
    SkillResult<T> run(String input);
}
