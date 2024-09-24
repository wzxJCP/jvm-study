package lang;

public class Student {

    @Override
    public java.lang.String toString() {
        return "Hello";
    }
    public static void main(String[] args) {
        Student student = new Student();
        System.out.println(student.toString());
    }
}