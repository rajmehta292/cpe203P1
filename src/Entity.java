import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public final class Entity
{
    public EntityKind kind;
    public String id;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int resourceLimit;
    public int resourceCount;
    public int actionPeriod;
    public int animationPeriod;

    public Entity(
            EntityKind kind,
            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int resourceCount,
            int actionPeriod,
            int animationPeriod)
    {
        this.kind = kind;
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public  boolean transformNotFull(

            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (resourceCount >= resourceLimit) {
            Entity miner = Functions.createMinerFull(id, resourceLimit,
                                           position, actionPeriod,
                                           animationPeriod,
                                           images);

            Functions.removeEntity(world, this);
            Functions.unscheduleAllEvents(scheduler, this);

            Functions.addEntity(world, miner);
            miner.scheduleActions(scheduler, world, imageStore);

            return true;
        }

        return false;
    }

    public  void transformFull(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        Entity miner = Functions.createMinerNotFull(id, resourceLimit,
                                          position, actionPeriod,
                                          animationPeriod,
                                          images);

        Functions.removeEntity(world, this);
        Functions.unscheduleAllEvents(scheduler, this);

        Functions.addEntity(world, miner);
        miner.scheduleActions(scheduler, world, imageStore);
    }

    public  boolean moveToNotFull(

            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (Functions.adjacent(position, target.position)) {
            resourceCount += 1;
            Functions.removeEntity(world, target);
            Functions.unscheduleAllEvents(scheduler, target);

            return true;
        }
        else {
            Point nextPos = nextPositionMiner( world, target.position);

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = Functions.getOccupant(world, nextPos);
                if (occupant.isPresent()) {
                    Functions.unscheduleAllEvents(scheduler, occupant.get());
                }

                Functions.moveEntity(world, this, nextPos);
            }
            return false;
        }
    }

    public  boolean moveToFull(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (Functions.adjacent(position, target.position)) {
            return true;
        }
        else {
            Point nextPos = nextPositionMiner( world, target.position);

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = Functions.getOccupant(world, nextPos);
                if (occupant.isPresent()) {
                    Functions.unscheduleAllEvents(scheduler, occupant.get());
                }

                Functions.moveEntity(world, this, nextPos);
            }
            return false;
        }
    }

    public  boolean moveToOreBlob(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (Functions.adjacent(position, target.position)) {
            Functions.removeEntity(world, target);
            Functions.unscheduleAllEvents(scheduler, target);
            return true;
        }
        else {
            Point nextPos = nextPositionOreBlob( world, target.position);

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = Functions.getOccupant(world, nextPos);
                if (occupant.isPresent()) {
                    Functions.unscheduleAllEvents(scheduler, occupant.get());
                }

                Functions.moveEntity(world, this, nextPos);
            }
            return false;
        }
    }

    public  void scheduleActions(

            EventScheduler scheduler,
            WorldModel world,
            ImageStore imageStore)
    {
        switch (kind) {
            case MINER_FULL:
                scheduler.scheduleEvent( this,
                              Functions.createActivityAction(this, world, imageStore),
                              this.actionPeriod);
                scheduler.scheduleEvent( this,
                              Functions.createAnimationAction(this, 0),
                              Functions.getAnimationPeriod(this));
                break;

            case MINER_NOT_FULL:
                scheduler.scheduleEvent( this,
                              Functions.createActivityAction(this, world, imageStore),
                              this.actionPeriod);
                scheduler.scheduleEvent( this,
                              Functions.createAnimationAction(this, 0),
                              Functions.getAnimationPeriod(this));
                break;

            case ORE:
                scheduler.scheduleEvent( this,
                              Functions.createActivityAction(this, world, imageStore),
                              this.actionPeriod);
                break;

            case ORE_BLOB:
                scheduler.scheduleEvent( this,
                              Functions.createActivityAction(this, world, imageStore),
                              this.actionPeriod);
                scheduler.scheduleEvent( this,
                              Functions.createAnimationAction(this, 0),
                              Functions.getAnimationPeriod(this));
                break;

            case QUAKE:
                scheduler.scheduleEvent( this,
                              Functions.createActivityAction(this, world, imageStore),
                              this.actionPeriod);
                scheduler.scheduleEvent( this, Functions.createAnimationAction(this,
                                                                       Functions.QUAKE_ANIMATION_REPEAT_COUNT),
                              Functions.getAnimationPeriod(this));
                break;

            case VEIN:
                scheduler.scheduleEvent( this,
                              Functions.createActivityAction(this, world, imageStore),
                              this.actionPeriod);
                break;

            default:
        }
    }

    public  Point nextPositionOreBlob(
             WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - position.x);
        Point newPos = new Point(position.x + horiz, position.y);

        Optional<Entity> occupant = Functions.getOccupant(world, newPos);

        if (horiz == 0 || (occupant.isPresent() && !(occupant.get().kind
                == EntityKind.ORE)))
        {
            int vert = Integer.signum(destPos.y - position.y);
            newPos = new Point(position.x, position.y + vert);
            occupant = Functions.getOccupant(world, newPos);

            if (vert == 0 || (occupant.isPresent() && !(occupant.get().kind
                    == EntityKind.ORE)))
            {
                newPos = position;
            }
        }

        return newPos;
    }

    public Point nextPositionMiner(
             WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - position.x);
        Point newPos = new Point(position.x + horiz, position.y);

        if (horiz == 0 || Functions.isOccupied(world, newPos)) {
            int vert = Integer.signum(destPos.y - position.y);
            newPos = new Point(position.x, position.y + vert);

            if (vert == 0 || Functions.isOccupied(world, newPos)) {
                newPos = position;
            }
        }

        return newPos;
    }
}
