
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentQLock<T> {

    AtomicInteger count = new AtomicInteger(0);
    ReentrantLock enqLock = new ReentrantLock();
    ReentrantLock deqLock = new ReentrantLock();
    Condition empty = deqLock.newCondition();

    Node<T> head;
    Node<T> tail;

    public ConcurrentQLock() {
        head = new Node<T>(null);
        tail = head;
    }

    public T deq()  {
        T ele;
        deqLock.lock();
        try {
        	
            //checks if there is only one node, then the Q is empty and deq is blocked.
    
        	 while (head.next == null ) {
                System.out.println("Deq blocked:empty Q");
                try {
					empty.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            ele = (T) head.next.ele;
            //deletes the sentinel node and makes the current node as sentinel node
            head = head.next;
            //decrements the atomic count
            count.decrementAndGet();
            System.out.println("Element dequed " + ele);
            System.out.println("Elements in Q after deque  " + count.get());
            System.out.println(" Deq printing " +printLinkedList());
        } finally {
            deqLock.unlock();
        }

        return ele;
    }
    public void enq(T x)
    {
        enqLock.lock();
        try {
            //inserts a new node and increments the atomic count.
            Node<T> y = new Node<T>(x);
            tail.next = y;
            tail = y;
            count.incrementAndGet();

            System.out.println("Element enqued " + x);
            System.out.println("Elements in Queue after enq " + count.get());
            System.out.println(" Enq printing " +printLinkedList());
			if (count.get() == 1) {
				try {
					deqLock.lock();
					empty.signal();

				}finally
				{
					deqLock.unlock();
				}
			}
        } finally {
            enqLock.unlock();
        }
    }

    private static class Node<T> {
        T ele;
        Node<T> next;

        Node(T ele ){
            this.ele = ele;
        }

    }

    private String printLinkedList()
    {
        StringBuffer listValues = new StringBuffer();
        if(head.next ==null)
        {
            listValues.append(" Empty list");
        }
        else
        {
            listValues.append(head.next.ele);
            Node next = head.next.next;
            while(next != null)
            {
                listValues.append(" , ").append(next.ele);
                Node prev = next;
                next = prev.next;
            }

        }
        return listValues.toString();
    }

}