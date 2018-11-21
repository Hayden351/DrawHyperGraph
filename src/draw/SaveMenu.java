import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.ArrayList; 
import static java.util.Arrays.asList; 
import java.util.regex.Pattern; 
import java.util.regex.Matcher; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SaveMenu extends PApplet {






// determine if points are ordered counter clock-wise
public boolean counterClockwise(PVector A,PVector B,PVector C)
{
  // slope of CA > slope of BA
  return (C.y-A.y) * (B.x-A.x) > (B.y-A.y) * (C.x-A.x);
}
// determines if AB intersects CD
public boolean doesIntersect(PVector A,PVector B,PVector C,PVector D)
{
  // 0   2 3: A is counter clockwise ordered to C and D
  // 1   2 3: B is counter clockwise ordered to C and D
  //          but not both counter clockwise ordered
  //   and
  // 0 1   2: C is counter clockwise ordered to A and B
  // 0 1   3: D is counter clockwise ordered to A and B
  //          but not both counter clockwise ordered
  // is sufficient to determine if two lines intersect 
  // except when they are parallel (not relevent here)
  return counterClockwise(A,B,C) ^ counterClockwise(A,B,D) && 
         counterClockwise(A,C,D) ^ counterClockwise(B,C,D);
}
// determines if the given point is inside the given polygon
public boolean isIn(PVector point, ArrayList<PVector> polygon)
{
  // parallel point at the edge of the canvas
  PVector extreme = new PVector(width,point.y);
  
  int intersectCount = 0;
  // counts the number of times the line from the point to  the extreme point 
  // intersects one of the sides of the polygon
  for (int currentPoint = 0, nextPoint = 1; 
                         currentPoint < polygon.size(); 
                             currentPoint++, nextPoint = (currentPoint + 1) % polygon.size())
    if (doesIntersect(polygon.get(currentPoint), 
                    polygon.get(nextPoint), point, extreme))
      intersectCount++;
  // inside iff point intersects an odd number of times
  return intersectCount % 2 == 1;
}
int fontSize = 13;

public void setup()
{
  

  setupSaveWindow();
  
  // sets font 
  textFont(createFont("Monospaced.plain", fontSize));
}
public void draw()
{
  // set white background
  background(255);
  
  drawSaveWindow();   
}
public void mousePressed()
{
  // if window is visible and mouse is on the window bar then select the window bar
  selectSaveWindowElement();
}
public void mouseReleased()
{   
  pressSaveWindowElements();
}
public void keyPressed(){}
public void keyReleased()
{
  // 
  switch (key)
  {
    // toggles the save window
    case 19:     // ctrl + s
      toggleWindow();
        break;
  }

  sendToSaveWindowTextBox(keyCode, key);
}




// save window elements
String saveText; // currently unused
MovableElement saveButton;

String closeText; // currently unused
MovableElement closeButton;

String barText;
MovableElement window;

// no text its a window
MovableElement windowBar;

StringBuilder textFieldText;
MovableElement textField;

String outputFieldText;
MovableElement outputField;

public void setupSaveWindow()
{
  // bar that when selected will move the entire window to the mouse
  windowBar = new MovableElement(width / 2, height / 2,new ArrayList<PVector>(asList(new PVector(-200,-50),new PVector(-200,25), new PVector(200,25),new PVector(200,-50))));
  barText = "Graph Save Menu";
  
  // rectangle that represents the window (not enforced)
  window = new MovableElement(0,0,new ArrayList<PVector>(asList(new PVector(-200,-200),new PVector(-200,200), new PVector(200,200),new PVector(200,-200))));
  
  // button that when pressed will close the window
  saveButton = new MovableElement(0,0,new ArrayList<PVector>(asList(new PVector(-fontSize,20),new PVector(70,20),new PVector(70,-30),new PVector(-fontSize,-30))));
  
  // button that when pressed will save the sketch into a folder
  closeButton = new MovableElement(0,0,new ArrayList<PVector>(asList(new PVector(-fontSize,20),new PVector(70,20),new PVector(70,-30),new PVector(-fontSize,-30))));
  
  // text field where user can enter the name they want to save
  textField = new MovableElement(0,0,new ArrayList<PVector>(asList(new PVector(-fontSize,25),new PVector(150,25),new PVector(150,-28),new PVector(-fontSize,-28))));
  textFieldText = new StringBuilder();
  
  // text field where output is given (sketch saved or error occured)
  outputField = new MovableElement(0,0,new ArrayList<PVector>(asList(new PVector(-fontSize,20),new PVector(400 - 3 * fontSize,20),new PVector(400 - 3 * fontSize,-30),new PVector(-fontSize,-30))));
  outputFieldText = "";
  
  // everything is attached to the window bar since the window bar is the only movable element
  windowBar.relatedElements.addAll(asList(
  new RelativeMovableElement(windowBar,window, new PVector(0,150)),
  new RelativeMovableElement(windowBar,saveButton, new PVector(-75,250)),
  new RelativeMovableElement(windowBar,closeButton,new PVector(75,250)),
  new RelativeMovableElement(windowBar,textField,new PVector(0,100)),
  new RelativeMovableElement(windowBar,outputField,new PVector(-200 + 2 * fontSize,175))
  ));
}


public void drawSaveWindow()
{
  // draw every element to the screen
  windowBar.drawShape();
  window.drawShape();
  saveButton.drawShape();
  closeButton.drawShape();
  textField.drawShape();
  outputField.drawShape();
  
  // if window is visible display text
  if (windowBar.visible)
  {
    textSize(fontSize);
    
    fill(0);
    //text(CENTER);
    text("Save as:",textField.x - 100, textField.y);
    text(textFieldText.toString(),textField.x,textField.y);
    text(outputFieldText,outputField.x,outputField.y);
    text(barText, windowBar.x - 175,windowBar.y);  
    text("Close",closeButton.x,closeButton.y);
    text("Save",saveButton.x,saveButton.y);
  }
}
// if toggles the visibility of the window and
// resets the window
public void toggleWindow ()
{
  windowBar.toPosition(width / 2,height / 4);
  windowBar.visible = !windowBar.visible;
  if (!windowBar.visible)
    windowBar.selected = false;
  for (RelativeMovableElement rme : windowBar.relatedElements)
  {
    rme.relativeElement.visible = !rme.relativeElement.visible;
  }  
  textFieldText = new StringBuilder();
  outputFieldText = "";
}
public void sendToSaveWindowTextBox(int code, Character regular)
{
  // delete characters in the text box
  if (code == BACKSPACE)
  {
    if (1 <= textFieldText.length())
      textFieldText.deleteCharAt(textFieldText.length() - 1);
  }
  
  // if the user types a key append to the text box
  else if (Character.isLetterOrDigit(regular))
  {
    if (textFieldText.length() <= 15)
      textFieldText.append(String.format("%c", key));
  }
}
public void selectSaveWindowElement()
{
  if (windowBar.visible)
  {
    if (windowBar.mouseIn())
    {
      windowBar.selected = true;
    }
  }
}

public void pressSaveWindowElements()
{
  if (windowBar.visible)
  {
    // if released on save button save the sketch
    if (saveButton.mouseIn())
    {
      // only save if there is text in the text box
      if (textFieldText.length() >= 1)
      {
        outputFieldText = String.format("Graph saved as %s.png\n",textFieldText);
        save(String.format("C:\\Users\\Hayden\\OneDrive\\Processing\\Images\\%s.png", textFieldText));
        
      }
      else
      {
        outputFieldText = String.format("Error no text in field");
      }
      
    }
    
    // close the window if the close button is pressed
    if (closeButton.mouseIn())
    {
      toggleWindow();
    }
  } // end if (windowBar.visible)
  
  // everything is deselected
  windowBar.selected = false;
}


/* unused code

  //textFont(createFont("Monospaced.plain", fontSize));
  // prints every font on this system
  //textFieldText = new StringBuilder();
  //for (Object obj : PFont.list())
  //  System.out.println(obj);
  // set font for sketch

  
  // whoops don't need this since the reserved characters are valid anyways
  //Pattern p = Pattern.compile("( < | > | : | \" | / | \\)");
  //if (!Pattern.matches(textFieldText.toString(),"[a-zA-Z_0-9]*( < | > | : | \" | / | \\)[a-zA-Z_0-9]*"))
      
  
*/






/* unused functions



float tolerance = 0.01;

enum Orientation
{
  COLLINEAR,
  CLOCKWISE,
  COUNTER_CLOCKWISE;
}

float distance (PVector v, PVector w)
{
  return (float)Math.sqrt(Math.pow(v.x - w.x,2) + Math.pow(v.y - w.y,2)); 
}

// determines the slope of 2 points
float slope (PVector v, PVector w)
{
  return (v.y - w.y) / (v.x - w.x);
}

void line (PVector aa, PVector bb)
{
  line(aa.x,aa.y,bb.x,bb.y);
}


// given 3 points determines their orientation relative to eachother
Orientation findOrientation (PVector aa, PVector bb, PVector cc)
{
  // slopes are equal (within tolerance) implies they are on the same line
  if (Math.abs(slope(aa,bb) - slope(bb,cc)) < tolerance)
  {
    return Orientation.COLLINEAR;
  }
  else if (slope(aa,bb) < slope(bb,cc))
  {
    return Orientation.CLOCKWISE;
  }
  else
  {
    return Orientation.COUNTER_CLOCKWISE;
  }
}

// determines if point p is on the segment A
boolean isOnSegment (PVector p, PVector AStart,PVector AEnd)
{
  if (findOrientation(p,AStart, AEnd) == Orientation.COLLINEAR)  
    if (min(AStart.x,AEnd.x) <= p.x && p.x <= max(AStart.x,AEnd.x) )
      if (min(AStart.y,AEnd.y) <= p.y && p.y <= max(AStart.y,AEnd.y) )
        return true;
  return false;
}


*/






// test functions
//  drawOrientationTest();
//  drawIntersectionTest();
  //drawIsOnSegmentTest();
  /*
void drawIsOnSegmentTest()
{
  
  PVector aa = new PVector(width / 2 + 0*scale,height / 2 + 0*scale);
  PVector bb = new PVector(width / 2 + 1*scale,height / 2 + 1*scale);
  PVector cc = new PVector(mouseX,mouseY);
  if (isOnSegment(cc,aa,bb))
  {
    stroke(0);
  }
  else
  {
    stroke(255,0,0);
  }
  
  line(aa,bb);
}

void drawIntersectionTest()
{
  intersectionCase(new PVector(width / 2 + scale * 0, height / 2 + scale * 0), new PVector(width / 2 + scale * 1,height / 2 + scale * 1),new PVector(width / 2 + 0*scale,height / 2 + 1*scale),new PVector(width / 2 + 1*scale,height / 2 + 0*scale));
  intersectionCase(new PVector(width / 4 + scale * 0, height / 2 + scale * 0), new PVector(width / 4 + scale * 1,height / 2 + scale * 1),new PVector(width / 4 + 0*scale,height / 2 + 1*scale),new PVector(width / 4 + 1*scale - 30,height / 2 + -4*scale));
  //intersectionCase(new PVector(width / 2 + scale * 0, height / 4 + scale * 0), new PVector(width / 2 + scale * 1,height / 4 + scale * 1),new PVector(width / 2 + 0*scale,height / 4 + 1*scale),new PVector(width / 2 - 1*scale,height / 4 + 0*scale));
  intersectionCase(new PVector(mouseX,mouseY), new PVector(width / 2 + scale * 1,height / 4 + scale * 1),new PVector(width / 2 + 0*scale,height / 4 + 1*scale),new PVector(width / 2 - 1*scale,height / 4 + 0*scale));
}
void intersectionCase(PVector a1, PVector a2, PVector b1, PVector b2)
{
 
  if (doesIntersect(a1,a2,b1,b2)) // intersecting lines are black
    stroke(0);
  else
    stroke(255,0,0); // non intersecting lines are red
  line(a1,a2);
  line(b1,b2);
}

void drawOrientationTest()
{
  drawCase(width / 2 + 0*scale, height / 2 + 0 * scale,width / 2 + 1*scale, height / 2 + 1 * scale,width / 2 + 2*scale, height / 2 + 2 * scale);
  drawCase(width / 4 + 0*scale, height / 2 + 0 * scale,width / 4 + 1*scale, height / 2 + 1 * scale,width / 4 + 2*scale, height / 2 + 2 * scale + 20);
  drawCase(width / 2 + 0*scale, height / 4 + 0 * scale,width / 2 + 1*scale, height / 4 + 1 * scale,width / 2 + 2*scale, height / 4 + 2 * scale - 20);
}
void drawCase(float a1,float a2,float b1,float b2,float c1,float c2)
{
  PVector aa = new PVector(a1,a2);
  PVector bb = new PVector(b1,b2);
  PVector cc = new PVector(c1,c2);
  
  switch(findOrientation(aa,bb,cc))
  {
    case COLLINEAR: // if points are collinear they are red
      stroke(255,0,0);
      break;
    case CLOCKWISE: // if points are clockwise oriented they are green
    stroke(0,255,0);
      break;
    case COUNTER_CLOCKWISE: // if points are counter-clockwise oriented the are blue
    stroke(0,0,255);
      break;
  }
  float rad = 5;
  ellipse(aa.x,aa.y,rad,rad);
  ellipse(bb.x,bb.y,rad,rad);
  ellipse(cc.x,cc.y,rad,rad);
}

boolean inRange(float x, float lBound, float rBound)
{
  return (lBound <= x && x <= rBound);
}


static void functionPVectorer(Runnable toRun) 
{
  toRun.run();
}
*/
class MovableElement
{
  float x;
  float y;
  ArrayList<PVector> relativePoints;
  boolean selected= false;
  boolean visible;
  ArrayList<RelativeMovableElement> relatedElements;
  
  MovableElement(float x, float y, ArrayList<PVector> rPVectors)
  {
    this.x = x;
    this. y = y;
    relativePoints = new ArrayList<PVector>(rPVectors);
    relatedElements = new ArrayList<RelativeMovableElement>();
    visible = false;
  }
  public void addMovableElement(RelativeMovableElement combo, PVector point)
  {
    for (RelativeMovableElement rme : combo.relativeElement.relatedElements)
    {
      relatedElements.add(rme);
    }
  }
  
  public void drawShape()
  {
    if (visible)
    {
      if (selected)
      {
        toPosition(mouseX,mouseY);
      }
      stroke(0);
      if (isIn(new PVector(mouseX - x,mouseY - y),relativePoints))
      {
        stroke(0,255,255);
        
      }
      else
      {
        stroke(0); 
      }
      
      for (int i = 1; i <= relativePoints.size(); i++)
      {
        line(x + relativePoints.get(i - 1).x,y + relativePoints.get(i - 1).y,
             x + relativePoints.get(i % relativePoints.size()).x,y + relativePoints.get(i % relativePoints.size()).y);
      }
      //line(x + relativePoints.get(relativePoints.size() - 1).x,y + relativePoints.get(relativePoints.size() - 1).y,
      //       x + relativePoints.get(0).x,y + relativePoints.get(0).y);
    }
  }
  public void toPosition(float xPos, float yPos)
  {
    x = constrain(xPos,0,width);
    y = constrain(yPos,0,height);
    for (RelativeMovableElement rme : relatedElements)
    {
      rme.relativeElement.x = constrain(xPos + rme.relativePosition.x,0,width);
      rme.relativeElement.y = constrain(yPos + rme.relativePosition.y,0,height);
    }
  }
  // returns true if the mouse is inside this element and false otherwise
  public boolean mouseIn()
  {
    return isIn(new PVector(mouseX - x,mouseY - y), relativePoints);
  } 
}
class RelativeMovableElement
{
  MovableElement relativeElement;
  PVector relativePosition;
  public RelativeMovableElement(MovableElement re, PVector rp)
  {
    this(re.x,re.y,re,rp);
  }
  public RelativeMovableElement(MovableElement me, MovableElement re, PVector rp)
  {
    this(me.x,me.y,re,rp);
  }
  public RelativeMovableElement(float x, float y, MovableElement re, PVector rp)
  {
    if (!(Math.abs(re.x - x) < 0.01f))
    {
      if (!(Math.abs(re.y - y) < 0.01f))
      {
        re.x = x;
        re.y = y;
        re.x += rp.x;
        re.y += rp.y;
      }
    }
    relativeElement = re;
    relativePosition = rp;
  }
}
  public void settings() {  size(1000,1000); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SaveMenu" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
