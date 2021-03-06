/*
Denna FSM används inte.

     _  _(o)_(o)_  _
   ._\`:_ F S M _:' \_,
       / (`---'\ `-.
    ,-`  _)    (_,

(not the Flying Spaghetti Monster, but ...)

A simple Finite State Machine library for Processing!

Based on the AlphaBeta FSM library for Arduino: http://www.arduino.cc/playground/Code/FiniteStateMachine
(Matches that API as closely as possible (with the exception of using string names for functions instead
of function pointers since function pointers are not available in Java).)

Learn more about Finite State Machines: http://en.wikipedia.org/wiki/Finite-state_machine

Usage:

Declare one FSM object and as many State objects as you like.

To initialize a State you need to pass in three strings representing the names of three functions
you've implemented in your sketch. These functions will be called when the state goes through its transitions:

State playMode = new State("enterPlayMode", "doPlayMode", "exitPlayMode");

The first function will be called once each time the FSM enters this state. (enter function)
The second function will be called repeated as long as the FSM stays in this state. (execute function)
The third function will be called one when the FSM transition away from this state. (exit function)

When you initialize the FSM object, you pass it the state you'd like it to begin in:

FSM game;

game = new FSM(startMode);

In draw(), call the game's update() function. This will ensure that the library calls the appropriate state's execute function.

To transition to a different state, call:

game.transitionTo(someState);

To retrieve the state the FSM is currently in, call:

game.getCurrentState();

To test if the game is in a current state, call:

if(game.isInState(someState){
 // do something
}


*/

/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FSM {
    State currentState;

    FSM(State initialState) {
        currentState = initialState;
    }

    void update() {
        currentState.executeFunction();
    }

    State getCurrentState(){
        return currentState;
    }

    boolean isInState(State state){
        return currentState == state;
    }

    void transitionTo(State newState) {
        currentState.exitFunction();
        currentState = newState;
        currentState.enterFunction();
    }
}


