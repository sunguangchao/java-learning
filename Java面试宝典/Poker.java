package iooc.timer;


/**
 * 关于内部类的使用练习
 * @author huang
 *
 */
public class Poker {
	private static String[] suites = {"黑桃","红桃","梅花","方块"};
	private static int[] faces = {1, 2, 3, 4 ,5 ,6, 7, 8, 9, 10,
			11, 12, 13};
	private Card[] cards;
	
	/**
	 * 构造器
	 *
	 */
	public Poker() {
		cards = new Card[52];
		for(int i = 0; i < suites.length; i++) {
			for(int j = 0; j < faces.length; j++) {
				cards[i*13+j] = new Card(suites[i], faces[j]);
			}
		}
	}
	/**
	 * 洗牌（随机乱序）
	 *
	 */
	public void shuffle() {
		for(int i=0, len = cards.length; i < len; i++) {
			int index = (int)(Math.random() * len);
			Card temp = cards[index];
			cards[index] = cards[i];
			cards[i] = temp;
		}
	}
	
	/**
	 * 发牌
	 * @param index 发牌的位置
	 *
	 */
	public Card deal(int index) {
		return cards[index];
	}
	
	
	public class Card{
		private String suite;//花色
		private int face;//点数
		
		public Card(String suite, int face) {
			this.suite = suite;
			this.face = face;
		}
		
		@Override
		public String toString() {
			String faceStr = "";
			switch (face) {
			case 1: faceStr = "A";
				break;
			case 11: faceStr = "J";
				break;
			case 12: faceStr = "Q";
				break;
			case 13: faceStr = "K";
				break;
			default:
				faceStr = String.valueOf(face);
			}
			return suite + faceStr;
		}
	}
	
	//测试
	public static void main(String[] args) {
		Poker poker = new Poker();
		poker.shuffle();//洗牌
		Poker.Card c1 = poker.deal(0);//发第一张牌
		//自己创建一张牌
		Poker.Card c2 = poker.new Card("红心", 1);
		System.out.println(c1);
		System.out.println(c2);
	}
	
	

}
/**
 * output:
 * 红桃8
 * 红心A
 */
