package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;

public class TimedQuoteMessenger {

    public interface Command { }
    public static final class Start implements Command { }
    private enum Timeout implements Command {
        INSTANCE
    }

    private static final Object TIMER_KEY = new Object();

    public static Behavior<Command> create(ActorRef<QuoteGenerator.GenerateQuote> target, Duration after) {
        return Behaviors.withTimers(timers -> new TimedQuoteMessenger(timers, target, after).idle());
    }

    private final TimerScheduler<Command> timers;
    private final ActorRef<QuoteGenerator.GenerateQuote> target;
    private final Duration after;

    private TimedQuoteMessenger(TimerScheduler<Command> timers, ActorRef<QuoteGenerator.GenerateQuote> target, Duration after) {
        this.timers = timers;
        this.target = target;
        this.after = after;
    }

    private Behavior<Command> idle() {
        return Behaviors.receive(Command.class)
                .onMessage(Command.class, this::onIdleCommand)
                .build();
    }

    private Behavior<Command> onIdleCommand(Command message) {
        timers.startTimerWithFixedDelay(TIMER_KEY, Timeout.INSTANCE, after);
        return Behaviors.setup(context -> new Messenger(context));
    }

    private class Messenger extends AbstractBehavior<Command> {

        Messenger(ActorContext<Command> context) {
            super(context);
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Timeout.class, message -> onTimeout())
                    .onMessage(Start.class, message -> onStart())
                    .build();
        }

        private Behavior<Command> onStart() {
            target.tell(new QuoteGenerator.GenerateQuote());
            return this;
        }

        private Behavior<Command> onTimeout() {
            target.tell(new QuoteGenerator.GenerateQuote());
            return idle();
        }
    }
}
