public final class Action
{
    public ActionKind kind;
    public Entity entity;
    public WorldModel world;
    public ImageStore imageStore;
    public int repeatCount;

    public Action(
            ActionKind kind,
            Entity entity,
            WorldModel world,
            ImageStore imageStore,
            int repeatCount)
    {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    public void executeAnimationAction(
            EventScheduler scheduler)
    {
        Functions.nextImage(entity);

        if (repeatCount != 1) {
            scheduler.scheduleEvent(entity,
                          Functions.createAnimationAction(entity,
                                                Math.max(repeatCount - 1,
                                                         0)),
                          Functions.getAnimationPeriod(entity));
        }
    }

    public void executeActivityAction(
            EventScheduler scheduler)
    {
        switch (entity.kind) {
            case MINER_FULL:
                scheduler.executeMinerFullActivity(entity, world,
                                         imageStore );
                break;

            case MINER_NOT_FULL:
                scheduler.executeMinerNotFullActivity(entity, world,
                                            imageStore);
                break;

            case ORE:
                scheduler.executeOreActivity(entity, world,
                                   imageStore);
                break;

            case ORE_BLOB:
                scheduler.executeOreBlobActivity(entity, world,
                                       imageStore);
                break;

            case QUAKE:
                scheduler.executeQuakeActivity(entity, world,
                                     imageStore);
                break;

            case VEIN:
                scheduler.executeVeinActivity(entity, world,
                                    imageStore);
                break;

            default:
                throw new UnsupportedOperationException(String.format(
                        "executeActivityAction not supported for %s",
                        entity.kind));
        }
    }

    public void executeAction( EventScheduler scheduler) {
        switch (kind) {
            case ACTIVITY:
                executeActivityAction(scheduler);
                break;

            case ANIMATION:
                executeAnimationAction(scheduler);
                break;
        }
    }
}
