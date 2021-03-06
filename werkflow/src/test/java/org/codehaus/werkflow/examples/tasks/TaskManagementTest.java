package org.codehaus.werkflow.examples.tasks;

import java.util.Properties;

import junit.framework.TestCase;

import org.codehaus.werkflow.Engine;
import org.codehaus.werkflow.InitialContext;
import org.codehaus.werkflow.Transaction;
import org.codehaus.werkflow.Workflow;
import org.codehaus.werkflow.idioms.interactive.Task;
import org.codehaus.werkflow.expressions.False;
import org.codehaus.werkflow.helpers.SimpleInstanceManager;
import org.codehaus.werkflow.helpers.SimplePersistenceManager;
import org.codehaus.werkflow.helpers.SimpleSatisfactionManager;
import org.codehaus.werkflow.helpers.SimpleWorkflowManager;
import org.codehaus.werkflow.simple.ActionManager;
import org.codehaus.werkflow.simple.ExpressionFactory;
import org.codehaus.werkflow.simple.SimpleWorkflowReader;
import org.codehaus.werkflow.spi.DefaultSatisfactionValues;
import org.codehaus.werkflow.spi.Expression;
import org.codehaus.werkflow.spi.Instance;
import org.codehaus.werkflow.spi.InstanceManager;
import org.codehaus.werkflow.spi.Path;
import org.codehaus.werkflow.spi.PersistenceManager;
import org.codehaus.werkflow.spi.RobustInstance;
import org.codehaus.werkflow.spi.SatisfactionManager;
import org.codehaus.werkflow.spi.SatisfactionSpec;
import org.codehaus.werkflow.spi.WorkflowManager;
import org.codehaus.werkflow.spi.SatisfactionValues;
import org.codehaus.werkflow.spi.Component;

public class TaskManagementTest extends TestCase
        implements ActionManager, ExpressionFactory
{
    private Engine engine;
    private Workflow workflow;

    protected void setUp() throws Exception
    {
        this.engine = createEngine();

        this.workflow = SimpleWorkflowReader.read(this,this,getClass().getResource("workflow.xml"));

        assertNotNull(this.workflow);

        this.engine.getWorkflowManager().addWorkflow(this.workflow);
    }

    public Engine createEngine()
    {
        PersistenceManager pm = new SimplePersistenceManager();
        WorkflowManager wm = new SimpleWorkflowManager();
        SatisfactionManager sm = new SimpleSatisfactionManager();
        InstanceManager im = new SimpleInstanceManager();

        Engine engine = new Engine();
        engine.setPersistenceManager(pm);
        engine.setSatisfactionManager(sm);
        engine.setWorkflowManager(wm);
        engine.setInstanceManager(im);

        engine.start();

        return engine;
    }

    public void testCreateWorkflowFromXML()
            throws Exception
    {
        Workflow articleWorkflow = this.engine.getWorkflowManager().getWorkflow("article");
        assertEquals(articleWorkflow, this.workflow);
    }


    public void testStartNewWorkflow()
            throws Exception
    {
        Workflow w = engine.getWorkflowManager().getWorkflow("article");

        InitialContext context = new InitialContext();
        context.set("subject", "Open source meals");

        // new user action creating a new instance
        Transaction tx = engine.beginTransaction(w.getId(), "request1", context);
        RobustInstance i = engine.getInstanceManager().getInstance(tx.getInstanceId());
        tx.commit();

        assertEquals("request1", i.getId());
        assertEquals("Open source meals", i.get("subject"));

        RobustInstance instance = engine.getInstanceManager().getInstance("request1");
        DefaultSatisfactionValues userInput;

        showWaitingTasks(instance);

        userInput = new DefaultSatisfactionValues();
        userInput.setValue("articleId", "32131231");
        userInput.setValue("author", "bart");
        finishTask(instance, "WriteArticle", userInput);

        showWaitingTasks(instance);

        userInput = new DefaultSatisfactionValues();
        userInput.setValue("advice", "reject");
        userInput.setValue("comment", "need more information");
        userInput.setValue("reviewer", "bob");
        finishTask(instance, "ReviewArticle1", userInput);

        showWaitingTasks(instance);

        userInput = new DefaultSatisfactionValues();
        userInput.setValue("advice", "proceed");
        userInput.setValue("comment", "good piece!");
        userInput.setValue("reviewer", "klaas");
        finishTask(instance, "ReviewArticle2", userInput);

        showWaitingTasks(instance);

        userInput = new DefaultSatisfactionValues();
        userInput.setValue("approved", "false");
        userInput.setValue("approver", "piet");
        finishTask(i, "ApproveArticle", userInput);

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException ie)
        {

        }        

        showWaitingTasks(instance);

        RobustInstance ii = engine.getInstanceManager().getInstance("request1");
        assertTrue(ii.isComplete());

        // lets stop the engine
        engine.stop();

        // make sure we cannot use the engine anymore
        // todo add test to see that engine is really stopped
    }


    // ********************************

    private void finishTask(RobustInstance i, String taskId, SatisfactionValues sv) throws Exception
    {
        System.out.println("User finishes task " + taskId + " and gives as input: ");
        String names[] = sv.getNames();
        for (int j = 0; j < names.length; j++)
        {
            String name = names[j];
            System.out.println( " - " + name + "=" + sv.getValue(name));
        }

        Transaction tx = engine.beginTransaction(i.getId());
        tx.satisfy(taskId, sv);
        tx.commit();
    }

    private void showWaitingTasks(RobustInstance i)
    {
        SatisfactionSpec[] specs = i.getEligibleSatisfactions();

        System.out.println(specs.length + " task item(s) waiting");

        for (int j = 0; j < specs.length; j++)
        {
            SatisfactionSpec spec = specs[j];
            System.out.println(" - " + spec.getId());

            Path path = i.getWorkflow().getSatisfactionPath(spec.getId());
            Component comp = i.getWorkflow().getComponent(path);
            if(Task.class.isAssignableFrom(comp.getClass()))
            {
                Task task = (Task)i.getWorkflow().getComponent(path);
                System.out.println("   task description = " + task.getTaskDescription());
                System.out.println("   assignee = " + task.getAssignee());
            }
        }
    }

    // ********************************

    public void perform(String actionId,
                        Instance instance,
                        Properties props)
            throws Exception
    {

    }


    public Expression newExpression(String text)
            throws Exception
    {
        return False.INSTANCE;
    }

}
