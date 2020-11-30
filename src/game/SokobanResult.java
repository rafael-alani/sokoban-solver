package game;

import java.io.*;
import java.time.LocalDateTime;

public class SokobanResult {
	private String id = null;
	private IAgent agent = null;
	private String level = null;
	private SokobanResultType result = null;
	private Throwable exception;
	private int steps = 0;
	private long simStartMillis = 0, simEndMillis = 0;
    public String message;

	/**
	 * Assigned ID given to this simulation.
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Agent that was running in simulation.
	 */
	public IAgent getAgent() {
		return agent;
	}

	public void setAgent(IAgent agent) {
		this.agent = agent;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * Result of the simulation.
	 */
	public SokobanResultType getResult() {
		return result;
	}

	public void setResult(SokobanResultType result) {
		this.result = result;
	}

	/**
	 * How many steps an agent performed.
	 */
	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	/**
	 * Time the simulation started in milliseconds (obtained via {@link System#currentTimeMillis()}.
	 */
	public long getSimStartMillis() {
		return simStartMillis;
	}

	public void setSimStartMillis(long simStartMillis) {
		this.simStartMillis = simStartMillis;
	}

	public void setSimEndMillis(long simEndMillis) {
		this.simEndMillis = simEndMillis;
	}
	
	/**
	 * How long the simulation run in milliseconds.
	 */
	public long getSimDurationMillis() {
		return simEndMillis - simStartMillis;
	}

	/**
	 * Exception caught during the simulation; 
	 * filled in case of {@link #getResult()} == {@link SokobanResultType#AGENT_EXCEPTION} or {@link SokobanResultType#SIMULATION_EXCEPTION}.  
	 */
	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}
	
	@Override
	public String toString() {
		return "SokobanResult[" + getResult() + "]";
	}
    
	public void outputResult(File resultFile, String levelFile, int level, String agentClassString) {
        boolean header = !resultFile.exists();
        
        try (FileOutputStream output = new FileOutputStream(resultFile, true);
             PrintWriter writer = new PrintWriter(output)) {
			if (header) {
				writer.println("datetime;id;levelFile;levelNumber;result;steps;playTimeMillis");
			}
            writer.println(LocalDateTime.now() + ";" + getId() + ";" +
                           new File(levelFile).getName() + ";" + level + ";" +
                           getResult() + ";" + getSteps() + ";" +
                           getSimDurationMillis());
		} catch (IOException e) {
            throw new RuntimeException("Failed to append to the result file: " +
                                       resultFile.getAbsolutePath());
		}
	}
}
