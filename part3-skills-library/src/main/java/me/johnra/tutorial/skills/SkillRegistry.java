package me.johnra.tutorial.skills;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SkillRegistry {

    private final Map<String, Skill<?>> skills = new ConcurrentHashMap<>();

    public SkillRegistry register(Skill<?> skill) {
        skills.put(skill.skillName(), skill);
        return this;
    }

    public Optional<Skill<?>> get(String name) {
        return Optional.ofNullable(skills.get(name));
    }

    public Collection<String> list() {
        return skills.keySet();
    }
}
