class DemoFactory extends ClientFactory {
    public ClientHandler make() {
        return new DemoClient();
    }
}
