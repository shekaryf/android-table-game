// Test to see if Intent works
Intent intent = new Intent(FirstPageSearch.this, FavoritesListPage.class);
System.out.println("Intent created: " + intent);
System.out.println("Component: " + intent.getComponent());
try {
    startActivity(intent);
    System.out.println("startActivity called successfully");
} catch (Exception e) {
    System.out.println("Error starting activity: " + e.getMessage());
    e.printStackTrace();
}
