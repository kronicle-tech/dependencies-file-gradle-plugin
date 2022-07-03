package tech.kronicle.dependenciesfile.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.TaskContainer;

public class DependenciesFilePlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        final TaskContainer tasks = target.getTasks();

        String projectName = target.toString();
        tasks.register(GenerateDependenciesFileTask.TASK_NAME, GenerateDependenciesFileTask.class, new GenerateDependenciesFileTaskAction(projectName));
    }

    private static class GenerateDependenciesFileTaskAction implements Action<GenerateDependenciesFileTask> {
        private final String projectName;

        public GenerateDependenciesFileTaskAction(String projectName) {
            this.projectName = projectName;
        }

        @Override
        public void execute(GenerateDependenciesFileTask task) {
            task.setDescription("Generates a gradle-dependencies.yaml file containing the details of all dependencies declared in " + projectName + ".");
            task.setGroup(BasePlugin.BUILD_GROUP);
            task.setImpliesSubProjects(false);
        }
    }
}
