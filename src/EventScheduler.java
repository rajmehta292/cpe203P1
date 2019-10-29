import java.util.*;

public final class EventScheduler
{
    public PriorityQueue<Event> eventQueue;
    public Map<Entity, List<Event>> pendingEvents;
    public double timeScale;

    public EventScheduler(double timeScale) {
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.pendingEvents = new HashMap<>();
        this.timeScale = timeScale;
    }

    public void executeMinerNotFullActivity(
            Entity entity,
            WorldModel world,
            ImageStore imageStore
            )
    {
        Optional<Entity> notFullTarget =
                Functions.findNearest(world, entity.position, EntityKind.ORE);

        if (!notFullTarget.isPresent() || !entity.moveToNotFull( world,
                                                         notFullTarget.get(),
                                                         this)
                || !entity.transformNotFull( world, this, imageStore))
        {
            scheduleEvent( entity,
                          Functions.createActivityAction(entity, world, imageStore),
                          entity.actionPeriod);
        }
    }

    public void executeOreActivity(
            Entity entity,
            WorldModel world,
            ImageStore imageStore
            )
    {
        Point pos = entity.position;

        Functions.removeEntity(world, entity);
        Functions.unscheduleAllEvents(this, entity);

        Entity blob = Functions.createOreBlob(entity.id + Functions.BLOB_ID_SUFFIX, pos,
                                    entity.actionPeriod / Functions.BLOB_PERIOD_SCALE,
                                    Functions.BLOB_ANIMATION_MIN + Functions.rand.nextInt(
                                            Functions.BLOB_ANIMATION_MAX
                                                    - Functions.BLOB_ANIMATION_MIN),
                                    Functions.getImageList(imageStore, Functions.BLOB_KEY));

        Functions.addEntity(world, blob);
        blob.scheduleActions( this, world, imageStore);
    }

    public  void executeOreBlobActivity(
            Entity entity,
            WorldModel world,
            ImageStore imageStore
            )
    {
        Optional<Entity> blobTarget =
                Functions.findNearest(world, entity.position, EntityKind.VEIN);
        long nextPeriod = entity.actionPeriod;

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().position;

            if (entity.moveToOreBlob( world, blobTarget.get(), this)) {
                Entity quake = Functions.createQuake(tgtPos,
                                           Functions.getImageList(imageStore, Functions.QUAKE_KEY));

                Functions.addEntity(world, quake);
                nextPeriod += entity.actionPeriod;
                quake.scheduleActions( this, world, imageStore);
            }
        }

        scheduleEvent( entity,
                      Functions.createActivityAction(entity, world, imageStore),
                      nextPeriod);
    }

    public  void executeQuakeActivity(
            Entity entity,
            WorldModel world,
            ImageStore imageStore
            )
    {
        Functions.unscheduleAllEvents(this, entity);
        Functions.removeEntity(world, entity);
    }

    public  void executeVeinActivity(
            Entity entity,
            WorldModel world,
            ImageStore imageStore
            )
    {
        Optional<Point> openPt = Functions.findOpenAround(world, entity.position);

        if (openPt.isPresent()) {
            Entity ore = Functions.createOre(Functions.ORE_ID_PREFIX + entity.id, openPt.get(),
                                   Functions.ORE_CORRUPT_MIN + Functions.rand.nextInt(
                                           Functions.ORE_CORRUPT_MAX - Functions.ORE_CORRUPT_MIN),
                                   Functions.getImageList(imageStore, Functions.ORE_KEY));
            Functions.addEntity(world, ore);
            ore.scheduleActions(this, world, imageStore);
        }

        scheduleEvent( entity,
                      Functions.createActivityAction(entity, world, imageStore),
                      entity.actionPeriod);
    }

    public  void executeMinerFullActivity(
            Entity entity,
            WorldModel world,
            ImageStore imageStore
            )
    {
        Optional<Entity> fullTarget =
                Functions.findNearest(world, entity.position, EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() && entity.moveToFull( world,
                                                 fullTarget.get(), this))
        {
            entity.transformFull( world, this, imageStore);
        }
        else {
            scheduleEvent( entity,
                          Functions.createActivityAction(entity, world, imageStore),
                          entity.actionPeriod);
        }
    }

    public void scheduleEvent(

            Entity entity,
            Action action,
            long afterPeriod)
    {
        long time = System.currentTimeMillis() + (long)(afterPeriod
                * timeScale);
        Event event = new Event(action, time, entity);

        eventQueue.add(event);

        // update list of pending events for the given entity
        List<Event> pending = pendingEvents.getOrDefault(entity,
                                                                   new LinkedList<>());
        pending.add(event);
        pendingEvents.put(entity, pending);
    }

    public  void updateOnTime( long time) {
        while (!eventQueue.isEmpty()
                && eventQueue.peek().time < time) {
            Event next = eventQueue.poll();

            Functions.removePendingEvent(this, next);

            next.action.executeAction( this);
        }
    }
}
