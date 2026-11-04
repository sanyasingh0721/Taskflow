package com.taskflow.config

import com.taskflow.project.Project
import com.taskflow.project.ProjectRepository
import com.taskflow.task.Priority
import com.taskflow.task.Task
import com.taskflow.task.TaskRepository
import com.taskflow.task.TaskStatus
import com.taskflow.user.AppUser
import com.taskflow.user.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * Adds a small demo team when the app starts: five users, eight projects and about forty tasks.
 * All demo accounts use DEMO_PASSWORD. Due dates are relative to today, so some tasks are
 * overdue, some are due soon, and some have no date.
 */
@Component
@ConditionalOnProperty(name = ["taskflow.seed-demo"], havingValue = "true")
class DemoDataSeeder(
    private val users: UserRepository,
    private val projects: ProjectRepository,
    private val tasks: TaskRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (users.existsByEmail(DEMO_EMAIL)) return

        val demo = createUser("Demo User", DEMO_EMAIL)
        val priya = createUser("Priya Sharma", "priya@taskflow.dev")
        val arjun = createUser("Arjun Mehta", "arjun@taskflow.dev")
        val neha = createUser("Neha Verma", "neha@taskflow.dev")
        val rahul = createUser("Rahul Iyer", "rahul@taskflow.dev")

        // Projects owned by Demo User. These are the ones you see after logging in as demo.
        seedWebsiteLaunch(demo, priya, arjun, neha)
        seedMobileApp(demo, arjun, rahul)
        seedCustomerOnboarding(demo, neha, priya)
        createProject("Internal Tools", "Backlog for internal dashboards. Nothing planned yet.", demo)

        // Projects owned by the other team members.
        seedDesignSystem(priya, neha, rahul)
        seedPaymentsMigration(arjun, rahul, priya, demo)
        seedHiringPlan(neha, priya, demo)
        seedBugBashSprint(rahul, arjun, priya)
    }

    private fun seedWebsiteLaunch(owner: AppUser, priya: AppUser, arjun: AppUser, neha: AppUser) {
        val project = createProject("Website Launch", "Marketing site relaunch for Q4", owner)
        saveTasks(
            newTask(project, "Finalise hero section copy", "Two headline options for A/B testing",
                TaskStatus.DONE, Priority.HIGH, -6, priya),
            newTask(project, "Set up analytics and conversion goals", null,
                TaskStatus.DONE, Priority.MEDIUM, -3, owner),
            newTask(project, "Design pricing page", "Three tiers with an annual billing toggle",
                TaskStatus.IN_PROGRESS, Priority.HIGH, 2, arjun),
            newTask(project, "Build contact form with validation", null,
                TaskStatus.IN_PROGRESS, Priority.MEDIUM, 5, neha),
            newTask(project, "Write SEO meta descriptions", "Cover the top 20 landing pages",
                TaskStatus.TODO, Priority.MEDIUM, -1, priya),
            newTask(project, "Legal review of privacy policy", "Waiting on sign-off from legal",
                TaskStatus.TODO, Priority.HIGH, 1, owner),
            newTask(project, "Optimise images for mobile", null,
                TaskStatus.TODO, Priority.LOW, 10, null),
        )
    }

    private fun seedMobileApp(owner: AppUser, arjun: AppUser, rahul: AppUser) {
        val project = createProject("Mobile App v2", "iOS and Android release, targeting December", owner)
        saveTasks(
            newTask(project, "Fix crash on iOS 18 startup", "Reproduced on iPhone 12 and newer",
                TaskStatus.DONE, Priority.HIGH, -2, arjun),
            newTask(project, "Login with biometrics", "Face ID and fingerprint, with password fallback",
                TaskStatus.IN_PROGRESS, Priority.HIGH, 4, rahul),
            newTask(project, "Push notification preferences", null,
                TaskStatus.TODO, Priority.MEDIUM, 9, rahul),
            newTask(project, "Offline mode for task lists", "Queue edits and sync when back online",
                TaskStatus.TODO, Priority.HIGH, 14, arjun),
            newTask(project, "Prepare App Store screenshots", "Update for the new navigation",
                TaskStatus.TODO, Priority.MEDIUM, -3, owner),
            newTask(project, "Dark mode for settings screen", null,
                TaskStatus.TODO, Priority.LOW, null, null),
        )
    }

    private fun seedCustomerOnboarding(owner: AppUser, neha: AppUser, priya: AppUser) {
        val project = createProject("Customer Onboarding Revamp", "Reduce drop-off during account setup", owner)
        saveTasks(
            newTask(project, "Map current onboarding funnel", "Identified three major drop-off points",
                TaskStatus.DONE, Priority.MEDIUM, -10, neha),
            newTask(project, "Draft welcome email series", "Five emails over the first two weeks",
                TaskStatus.IN_PROGRESS, Priority.MEDIUM, 6, priya),
            newTask(project, "Interactive setup checklist", "Shows progress and suggests next steps",
                TaskStatus.TODO, Priority.HIGH, 12, neha),
            newTask(project, "Measure drop-off at step 3", null,
                TaskStatus.TODO, Priority.MEDIUM, 3, owner),
            newTask(project, "Retire legacy tour modal", null,
                TaskStatus.TODO, Priority.LOW, null, null),
        )
    }

    private fun seedDesignSystem(priya: AppUser, neha: AppUser, rahul: AppUser) {
        val project = createProject("Design System 2.0", "Shared components for web and mobile", priya)
        saveTasks(
            newTask(project, "Define colour and spacing tokens", null,
                TaskStatus.DONE, Priority.HIGH, -8, priya),
            newTask(project, "Accessible button and input variants", "WCAG AA contrast for all states",
                TaskStatus.IN_PROGRESS, Priority.HIGH, 3, neha),
            newTask(project, "Modal and drawer components", null,
                TaskStatus.TODO, Priority.MEDIUM, 8, rahul),
            newTask(project, "Document usage guidelines", null,
                TaskStatus.TODO, Priority.LOW, -2, null),
            newTask(project, "Review focus styles with QA", null,
                TaskStatus.IN_PROGRESS, Priority.MEDIUM, 2, rahul),
            newTask(project, "Add dark theme tokens", null,
                TaskStatus.TODO, Priority.MEDIUM, null, priya),
        )
    }

    private fun seedPaymentsMigration(owner: AppUser, rahul: AppUser, priya: AppUser, demo: AppUser) {
        val project = createProject("Payments API Migration", "Move mobile clients to the v2 payments API", owner)
        saveTasks(
            newTask(project, "Inventory existing payment endpoints", "Forty-two endpoints across three services",
                TaskStatus.DONE, Priority.HIGH, -12, owner),
            newTask(project, "Write migration plan for older clients", null,
                TaskStatus.IN_PROGRESS, Priority.HIGH, 4, priya),
            newTask(project, "Add idempotency keys to charge endpoint", "Prevents double charges on retry",
                TaskStatus.TODO, Priority.HIGH, 7, owner),
            newTask(project, "Load test new gateway", null,
                TaskStatus.TODO, Priority.MEDIUM, -1, rahul),
            newTask(project, "Update public API docs", null,
                TaskStatus.TODO, Priority.LOW, null, demo),
        )
    }

    private fun seedHiringPlan(owner: AppUser, priya: AppUser, demo: AppUser) {
        val project = createProject("Q4 Hiring Plan", "Two frontend and one backend engineer", owner)
        saveTasks(
            newTask(project, "Define frontend engineer role", "Level, scope and salary band agreed",
                TaskStatus.DONE, Priority.MEDIUM, -15, owner),
            newTask(project, "Review take-home submissions", "Use the rubric from the hiring doc",
                TaskStatus.IN_PROGRESS, Priority.MEDIUM, 5, owner),
            newTask(project, "Shortlist candidates for interviews", null,
                TaskStatus.IN_PROGRESS, Priority.HIGH, 2, priya),
            newTask(project, "Schedule panel interviews", null,
                TaskStatus.TODO, Priority.HIGH, 9, null),
            newTask(project, "Send offer letter template to HR", null,
                TaskStatus.TODO, Priority.LOW, -3, demo),
        )
    }

    private fun seedBugBashSprint(owner: AppUser, arjun: AppUser, priya: AppUser) {
        val project = createProject("Bug Bash Sprint", "Clear the bug backlog before the release freeze", owner)
        saveTasks(
            newTask(project, "Triage open bug reports", "Sorted by severity and reproducibility",
                TaskStatus.DONE, Priority.HIGH, -5, owner),
            newTask(project, "Reproduce login timeout on Safari", null,
                TaskStatus.IN_PROGRESS, Priority.HIGH, 1, arjun),
            newTask(project, "Fix broken date picker in task form", null,
                TaskStatus.TODO, Priority.MEDIUM, 3, owner),
            newTask(project, "Check empty state copy on board", null,
                TaskStatus.TODO, Priority.LOW, 14, null),
            newTask(project, "Sign off on release candidate", null,
                TaskStatus.TODO, Priority.HIGH, 6, priya),
        )
    }

    private fun createUser(name: String, email: String): AppUser = users.save(
        AppUser(name = name, email = email, passwordHash = passwordEncoder.encode(DEMO_PASSWORD)),
    )

    private fun createProject(name: String, description: String, owner: AppUser): Project =
        projects.save(Project(name = name, description = description, owner = owner))

    /** dueInDays is relative to today: negative means overdue, null means no due date. */
    private fun newTask(
        project: Project,
        title: String,
        description: String?,
        status: TaskStatus,
        priority: Priority,
        dueInDays: Long?,
        assignee: AppUser?,
    ): Task = Task(
        project = project,
        title = title,
        description = description,
        status = status,
        priority = priority,
        dueDate = dueInDays?.let { LocalDate.now().plusDays(it) },
        assignee = assignee,
    )

    private fun saveTasks(vararg items: Task) {
        tasks.saveAll(items.toList())
    }

    companion object {
        const val DEMO_EMAIL = "demo@taskflow.dev"
        const val DEMO_PASSWORD = "password123"
    }
}
