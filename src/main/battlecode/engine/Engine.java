package battlecode.engine;

import battlecode.engine.instrumenter.*;
import battlecode.engine.instrumenter.lang.RoboRandom;
import battlecode.engine.scheduler.Scheduler;
import battlecode.engine.signal.Signal;
import battlecode.common.*;
import battlecode.world.GameWorldFactory;
import battlecode.server.Config;

//~ import java.lang.Thread;
/*
TODO:
 - constructor
 - comments
*/

public class Engine {

	private final GenericWorld gameWorld;
		
	private final boolean garbageCollectEnabled;
	private final int garbageCollectRounds;
	private final boolean breakpointsEnabled;
		
	private static Engine theInstance = null;

	public Engine(String teamA, String teamB, String mapName, String mapPath, long[][] archonMemory) {
		theInstance = this;
		Config options = Config.getGlobalConfig();
		this.garbageCollectEnabled = options.getBoolean("bc.engine.gc");
		this.garbageCollectRounds = options.getInt("bc.engine.gc-rounds");
		this.breakpointsEnabled = options.getBoolean("bc.engine.breakpoints");
		GenericWorld tempGameWorld = null;
		//InternalObject.resetIDs();
		IndividualClassLoader.reset();
		Scheduler.reset();
		RobotMonitor.reset();
		PlayerFactory.checkOptions();
		try{
			try{
				tempGameWorld = GameWorldFactory.createGameWorld(teamA, teamB, mapName, mapPath, archonMemory);
			} catch(IllegalArgumentException e) {
				java.lang.System.out.println("[Engine] Error while loading map '" + mapName + "'");
				return;
			} catch(Exception e) {
				ErrorReporter.report(e);
				return;
			}
		} finally {
			gameWorld = tempGameWorld;
		}
		gameWorld.resetStatic();
		RobotMonitor.setGameWorld(gameWorld);
		RoboRandom.setMapSeed(gameWorld.getMapSeed());
		Scheduler.start();
	}
		
	public GameWorldViewer getGameWorldViewer() {
		return gameWorld;
	}
	
	public GameState runRound() {
		if(gameWorld == null)
			return GameState.DONE;
		if(!gameWorld.isRunning()) {
			return GameState.DONE;
		}
		try {
			if(gameWorld.getCurrentRound() != -1)
				gameWorld.clearAllSignals();
			gameWorld.processBeginningOfRound();
			if (Clock.getRoundNum() % 500 == 0)
			    System.out.println("Round: " + Clock.getRoundNum());
			Scheduler.passToNextThread();
			gameWorld.processEndOfRound();
			if(!gameWorld.isRunning()) {
				// Let all of the threads return so we don't leak
				// memory.  GameWorld has already told RobotMonitor
				// to kill all the robots;
				//System.out.println("Trying to clean up robots");
				Scheduler.passToNextThread();
			}
		} catch(Exception e) {
			ErrorReporter.report(e);
			return GameState.DONE;
		}
		if(garbageCollectEnabled && Clock.getRoundNum() % garbageCollectRounds == 0)
			java.lang.System.gc();
		return ((breakpointsEnabled && gameWorld.wasBreakpointHit()) ? GameState.BREAKPOINT : GameState.RUNNING);
	}
	
	/**
	 * TODO: update this, since energon change signal breaks this
	 * @return true if the gamestate may have changed, false if the gamestate did not change
	 */
	public boolean receiveSignal(Signal s) {
		gameWorld.clearAllSignals();
		Exception result = s.accept(gameWorld);
		return (result == null);
	}
	
	public boolean isRunning() {
		return (gameWorld != null) && gameWorld.isRunning();
	}
	
	public static int getRoundNum() {
		return theInstance.gameWorld.getCurrentRound();
	}
	
	public long[][] getArchonMemory() {
		return gameWorld.getArchonMemory();
	}
		
}
