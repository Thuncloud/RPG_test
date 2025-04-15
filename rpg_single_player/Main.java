import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        GameContext context = GameInitializer.init();
        GameEngine engine = new GameEngine(context);
        engine.start();
    }
}

public class GameContext {
    private Player player;
    private Room currentRoom;

    public GameContext(Player player, Room startRoom) {
        this.player = player;
        this.currentRoom = startRoom;
    }

    public Player getPlayer() { return player; }
    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }
}

public class GameEngine {
    private GameContext context;
    private Scanner scanner = new Scanner(System.in);

    public GameEngine(GameContext context) {
        this.context = context;
    }

    public void start() {
        while (context.getPlayer().isAlive()) {
            System.out.println("\n你目前在：" + context.getCurrentRoom().getName());
            System.out.println("當前角色狀況：" + context.getPlayer().getHp() + "/100");
            System.out.print("> ");
            //輸入指令
            String input = scanner.nextLine();
            //解析指令
            Command command = CommandParser.parse(input);
            if (command != null) {
                command.execute(context.getPlayer(), context);
            } else {
                /*System.out.println("無效的指令。");*/
                break;
            }
        }
        System.out.println("你已死亡，遊戲結束。");
    }
}

public class GameInitializer {
    public static GameContext init() {
        //房間資訊
        Room forest = new Room("森林入口", "你站在茂密森林的邊緣", new Monster("哥布林", 30, 8), true);
        Room temple = new Room("神殿大廳", "光線從破碎的石窗灑落", new Monster("亡靈戰士", 50, 12), false);
        //房間出口設置
        forest.setExit("north", temple);
        temple.setExit("south", forest);
        //玩家資訊設置
        Player player = new Player("勇者", 100, 15);
        return new GameContext(player, forest);
    }
}

public class Player {
    private String name;
    private int hp;
    private int attack;
    private int killCount = 0;
    private int totalDamage = 0;

    public Player(String name, int hp, int attack) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
    }

    public boolean isAlive() { return hp > 0; }
    public void takeDamage(int dmg) { hp -= dmg; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public void addKill() { killCount++; }
    public void addDamage(int dmg) { totalDamage += dmg; }
}

public class Room {
    private String name;
    private String description;
    private Monster monster;
    private boolean hasPotion;
    private Map<String, Room> exits = new HashMap<>();

    public Room(String name, String description, Monster monster, boolean hasPotion) {
        this.name = name;
        this.description = description;
        this.monster = monster;
        this.hasPotion = hasPotion;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Monster getMonster() { return monster; }
    public boolean hasPotion() { return hasPotion; }
    public void removePotion() { hasPotion = false; }
    public void setExit(String direction, Room room) { exits.put(direction, room); }
    public Room getExit(String direction) { return exits.get(direction); }
    public String getExitString() { return String.join(", ", exits.keySet()); }
}

public class Monster {
    private String name;
    private int hp;
    private int attack;

    public Monster(String name, int hp, int attack) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
    }

    public String getName() { return name; }
    public boolean isAlive() { return hp > 0; }
    public void takeDamage(int damage) { hp -= damage; }
    public int getAttack() { return attack; }
    public int getHp() { return hp; }
}

public interface Command {
    void execute(Player player, GameContext context);
}

public class CommandParser {
    public static Command parse(String input) {
        if (input.startsWith("move ")) return new MoveCommand(input.substring(5));
        if (input.equals("attack")) return new AttackCommand();
        if (input.equals("look")) return new LookCommand();
        if (input.equals("use potion")) return new UsePotionCommand();
        if (input.startsWith("skill ")) return new SkillCommand(input.substring(6));
        return null;
    }
}

public class MoveCommand implements Command {
    private String direction;

    public MoveCommand(String direction) {
        //儲存解析移動指令後產生的移動方向
        this.direction = direction;
    }

    public void execute(Player p, GameContext c) {
        //根據解析結果尋找房間的出口，如果是NULL則沒有出口
        Room nextRoom = c.getCurrentRoom().getExit(direction);
        if (nextRoom != null) {
            c.setCurrentRoom(nextRoom);
            System.out.println("你移動到了：" + nextRoom.getName());
        } else {
            System.out.println("這個方向沒有路。");
        }
    }
}

public interface Skill { 
    String getName(); 
    void use(Player player, Monster monster); 
}

public class SkillCommand implements Command { 
    public SkillCommand(String name) {

    }
    public void execute(Player p, GameContext c) {

    } 
}

public class UsePotionCommand implements Command {
    public void execute(Player p, GameContext c) {
        Room r = c.getCurrentRoom();
        //如果有藥水則使用
        if (r.hasPotion()) {
            p.addDamage(-30); // 回復等於 - 傷害
            r.removePotion();
            System.out.println("你使用了一瓶藥水，恢復了 30 點生命！");
        } else {
            System.out.println("這裡沒有藥水可用。");
        }
    }
}


public class LookCommand implements Command {
    public void execute(Player p, GameContext c) {
        Room room = c.getCurrentRoom();
        System.out.println("\n你觀察四周：");
        //房間出口
        System.out.println(room.getDescription());
        System.out.println("出口方向：" + room.getExitString());
        //道具資訊
        if (room.hasPotion()) System.out.println("你看到一瓶治癒藥水。");
        //怪物資訊
        if (room.getMonster() != null && room.getMonster().isAlive()) {
            System.out.println("一隻 " + room.getMonster().getName() + " 擋住了你的去路！");
        }
    }
}


public class AttackCommand implements Command {
    public void execute(Player p, GameContext c) {
        Monster m = c.getCurrentRoom().getMonster();
        //怪物生存判定
        if (m != null && m.isAlive()) {
            //怪物受到的傷害(玩家造成的傷害)
            m.takeDamage(p.getAttack());
            //玩家在整場遊戲中造成的總傷害(+本次行動所造成的傷害)
            p.addDamage(p.getAttack());
            
            //"玩家攻擊"的文字描述
            System.out.println("你攻擊了 " + m.getName() + "，造成 " + p.getAttack() + " 傷害。");
            
            //怪物生存判定
            if (!m.isAlive()) {
                System.out.println(m.getName() + " 被擊敗了！");
                p.addKill();  //增加玩家擊殺數
            } else {
                //玩家受到的傷害(怪物造成的傷害)
                p.takeDamage(m.getAttack());
                //"怪物攻擊"的文字描述
                System.out.println(m.getName() + " 反擊了你，造成 " + m.getAttack() + " 傷害。");
            }
        } else {
            System.out.println("這裡沒有怪物或怪物已經死亡。");
        }
    }
}

/*java Main.java*/
